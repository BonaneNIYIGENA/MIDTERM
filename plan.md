# Specialist Doctor Booking System
### Complete Implementation Plan — Spring Boot + PostgreSQL

---

## PART 1 — REQUIREMENTS

### 1.1 System Description
A scheduling management system between patients and specialist doctors. Doctors set their availability schedules. Patients search for doctors and book available slots. Doctors manage appointments and write post-consultation reports.

### 1.2 Exam Requirements Coverage

| Requirement | Solution | Where |
|---|---|---|
| ERD with 5+ tables | 6 tables + 1 join table = 7 total | Database design |
| Save Location | Location table: province, district, sector, cell, village | Location entity |
| One-to-One | Appointment → AppointmentReport | AppointmentReport entity |
| One-to-Many | Location → Users, Location → Doctors, Doctor → Schedules, Doctor → Appointments, User → Appointments | Multiple entities |
| Many-to-Many | Doctor ↔ User (regular patients) via doctor_patient join table | Doctor entity |
| existBy() | `UserRepository.existsByEmail(String email)` | UserRepository |
| Pagination | `PageRequest.of(page, size)` on GET /api/doctors | DoctorServiceImpl |
| Sorting | `Sort.by(sortBy).ascending()` on GET /api/doctors | DoctorServiceImpl |
| Query by Province | `findByLocation_Province(String province)` | UserRepository |

### 1.3 User Stories

**Patient (User)**
- Register with name, email, phone and location
- Browse doctors filtered by specialty or province
- View available schedule slots for a doctor
- Book an available slot
- View my appointments

**Doctor**
- Register with specialty, experience, hospital and location
- Create schedule slots with date, start time and end time
- View booked appointments
- Confirm or cancel an appointment
- Write a post-consultation report after a completed appointment
- Manually add a patient as a regular patient

---

## PART 2 — DATABASE DESIGN

### 2.1 Tables Overview

| Table | Purpose | Key Columns |
|---|---|---|
| locations | Full address hierarchy for users and doctors | id, province, district, sector, cell, village |
| users | Patient accounts | id, name, email, phone, address, location_id (FK) |
| doctors | Doctor profiles | id, name, email, specialty, experienceYears, hospitalName, hospitalAddress, location_id (FK) |
| schedules | Time slots created by doctors | id, doctor_id (FK), date, startTime, endTime, isBooked |
| appointments | Bookings made by patients | id, user_id (FK), doctor_id (FK), schedule_id (FK), status, notes |
| appointment_reports | Post-consultation report per appointment | id, appointment_id (FK unique), diagnosis, prescription, followUpDate, remarks |
| doctor_patient (join) | Tracks which patients a doctor regularly consults | doctor_id (FK), user_id (FK) |

### 2.2 Relationships

| Type | Between | Detail |
|---|---|---|
| One-to-Many | Location → Users | One location has many users |
| One-to-Many | Location → Doctors | One location has many doctors |
| One-to-Many | Doctor → Schedules | One doctor creates many schedule slots |
| One-to-Many | Doctor → Appointments | One doctor has many appointments |
| One-to-Many | User → Appointments | One user has many appointments |
| Many-to-Many | Doctor ↔ User | Doctor saves regular patients via doctor_patient join table |
| One-to-One | Appointment → AppointmentReport | One appointment has one post-consultation report |

### 2.3 Entity Relationship Diagram (Text)

```
locations ||--o{ users            : "has many"
locations ||--o{ doctors          : "has many"
doctors   ||--o{ schedules        : "creates"
doctors   ||--o{ appointments     : "receives"
users     ||--o{ appointments     : "makes"
appointments ||--|| appointment_reports : "has one"
doctors   }o--o{ users            : "regular patients (doctor_patient)"
```

---

## PART 3 — PROJECT SETUP

### 3.1 Spring Initializr — https://start.spring.io

| Field | Value |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.x.x latest stable |
| Packaging | Jar |
| Java | 17 or 21 |
| Group | com |
| Artifact | specialistbooking |
| Package name | com.specialistbooking |

**Dependencies:**
- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Lombok
- Validation

### 3.2 pom.xml — Add dotenv dependency

```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```

### 3.3 .env (project root — never commit this)

```env
DB_URL=jdbc:postgresql://localhost:5432/specialist_booking
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
```

> Add `.env` to `.gitignore` immediately.

### 3.4 application.properties

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

server.port=8080
```

### 3.5 Package Structure

```
com.specialistbooking
├── entity/
│   ├── Location.java
│   ├── User.java
│   ├── Doctor.java
│   ├── Schedule.java
│   ├── Appointment.java
│   └── AppointmentReport.java
├── enums/
│   └── AppointmentStatus.java
├── repository/
│   ├── LocationRepository.java
│   ├── UserRepository.java
│   ├── DoctorRepository.java
│   ├── ScheduleRepository.java
│   ├── AppointmentRepository.java
│   └── AppointmentReportRepository.java
├── service/
│   ├── LocationService.java
│   ├── UserService.java
│   ├── DoctorService.java
│   ├── ScheduleService.java
│   └── AppointmentService.java
├── serviceImpl/
│   ├── LocationServiceImpl.java
│   ├── UserServiceImpl.java
│   ├── DoctorServiceImpl.java
│   ├── ScheduleServiceImpl.java
│   └── AppointmentServiceImpl.java
├── controller/
│   ├── LocationController.java
│   ├── UserController.java
│   ├── DoctorController.java
│   ├── ScheduleController.java
│   └── AppointmentController.java
├── dto/
│   ├── request/
│   │   ├── UserRequest.java
│   │   ├── DoctorRequest.java
│   │   ├── ScheduleRequest.java
│   │   ├── AppointmentRequest.java
│   │   └── AppointmentReportRequest.java
│   └── response/
│       ├── UserResponse.java
│       └── DoctorResponse.java
└── exception/
    └── ResourceNotFoundException.java
```

---

## PART 4 — CODING

> **Build in this exact order to avoid compilation errors:**
> 1. Enum
> 2. Entities (Location → User → Doctor → Schedule → Appointment → AppointmentReport)
> 3. Exception
> 4. Repositories
> 5. Service Interfaces
> 6. Service Implementations
> 7. Controllers
> 8. DTOs

---

### 4.1 AppointmentStatus.java — `enums/`

```java
package com.specialistbooking.enums;

public enum AppointmentStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}
```

---

### 4.2 Location.java — `entity/`

> **PURPOSE:** Stores full address hierarchy. Both users and doctors reference this table via location_id FK. Satisfies location saving and province query requirements.

```java
package com.specialistbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String sector;

    @Column(nullable = false)
    private String cell;

    @Column(nullable = false)
    private String village;

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Doctor> doctors = new ArrayList<>();
}
```

---

### 4.3 User.java — `entity/`

> **PURPOSE:** Core patient entity. ManyToOne to Location. Inverse side of ManyToMany with Doctor. OneToMany to Appointments. existsByEmail runs against this table.

```java
package com.specialistbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToMany(mappedBy = "regularPatients")
    @JsonIgnore
    private List<Doctor> doctors = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Appointment> appointments = new ArrayList<>();
}
```

---

### 4.4 Doctor.java — `entity/`

> **PURPOSE:** Core doctor entity. Owns ManyToMany with User via doctor_patient join table. OneToMany to Schedules and Appointments. ManyToOne to Location. Specialty stored as plain text field.

```java
package com.specialistbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String specialty;

    private int experienceYears;
    private String hospitalName;
    private String hospitalAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToMany
    @JoinTable(
        name = "doctor_patient",
        joinColumns = @JoinColumn(name = "doctor_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<User> regularPatients = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Appointment> appointments = new ArrayList<>();
}
```

---

### 4.5 Schedule.java — `entity/`

> **PURPOSE:** Doctor creates time slots. isBooked flips to true when a patient books it, making it unavailable to others.

```java
package com.specialistbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private boolean isBooked = false;
}
```

---

### 4.6 Appointment.java — `entity/`

> **PURPOSE:** Created when a patient books a schedule slot. Links User, Doctor and Schedule. Doctor updates status. Inverse side of OneToOne with AppointmentReport.

```java
package com.specialistbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.specialistbooking.enums.AppointmentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    private String notes;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    @JsonIgnore
    private AppointmentReport report;
}
```

---

### 4.7 AppointmentReport.java — `entity/`

> **PURPOSE:** Satisfies the One-to-One requirement. Written by doctor after consultation. Unique FK on appointment_id enforces exactly one report per appointment.

```java
package com.specialistbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointment_reports")
public class AppointmentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true, nullable = false)
    private Appointment appointment;

    @Column(nullable = false)
    private String diagnosis;

    private String prescription;
    private LocalDate followUpDate;
    private String remarks;
}
```

---

### 4.8 ResourceNotFoundException.java — `exception/`

```java
package com.specialistbooking.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

---

### 4.9 Repositories — `repository/`

**LocationRepository.java**
```java
package com.specialistbooking.repository;

import com.specialistbooking.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByProvince(String province);
}
```

**UserRepository.java**
```java
package com.specialistbooking.repository;

import com.specialistbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    // Satisfies existBy() requirement
    boolean existsByEmail(String email);

    // Satisfies query by province requirement
    List<User> findByLocation_Province(String province);
}
```

**DoctorRepository.java**
```java
package com.specialistbooking.repository;

import com.specialistbooking.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Page<Doctor> findAll(Pageable pageable);
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
    List<Doctor> findByLocation_Province(String province);
}
```

**ScheduleRepository.java**
```java
package com.specialistbooking.repository;

import com.specialistbooking.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByDoctor_IdAndIsBookedFalse(Long doctorId);
    List<Schedule> findByDoctor_IdAndDate(Long doctorId, LocalDate date);
}
```

**AppointmentRepository.java**
```java
package com.specialistbooking.repository;

import com.specialistbooking.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctor_Id(Long doctorId);
    List<Appointment> findByUser_Id(Long userId);
}
```

**AppointmentReportRepository.java**
```java
package com.specialistbooking.repository;

import com.specialistbooking.entity.AppointmentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppointmentReportRepository extends JpaRepository<AppointmentReport, Long> {
    Optional<AppointmentReport> findByAppointment_Id(Long appointmentId);
}
```

---

### 4.10 Service Interfaces — `service/`

**LocationService.java**
```java
package com.specialistbooking.service;

import com.specialistbooking.entity.Location;
import java.util.List;

public interface LocationService {
    Location createLocation(Location location);
    List<Location> getAllLocations();
}
```

**UserService.java**
```java
package com.specialistbooking.service;

import com.specialistbooking.dto.request.UserRequest;
import com.specialistbooking.entity.User;
import java.util.List;

public interface UserService {
    User registerUser(UserRequest request);
    List<User> getUsersByProvince(String province);
    User getUserById(Long id);
}
```

**DoctorService.java**
```java
package com.specialistbooking.service;

import com.specialistbooking.dto.request.DoctorRequest;
import com.specialistbooking.entity.Doctor;
import org.springframework.data.domain.Page;
import java.util.List;

public interface DoctorService {
    Doctor addDoctor(DoctorRequest request);
    Page<Doctor> getDoctors(int page, int size, String sortBy);
    Doctor addRegularPatient(Long doctorId, Long userId);
    List<Doctor> getDoctorsBySpecialty(String specialty);
}
```

**ScheduleService.java**
```java
package com.specialistbooking.service;

import com.specialistbooking.dto.request.ScheduleRequest;
import com.specialistbooking.entity.Schedule;
import java.util.List;

public interface ScheduleService {
    Schedule createSchedule(ScheduleRequest request);
    List<Schedule> getAvailableSlots(Long doctorId);
}
```

**AppointmentService.java**
```java
package com.specialistbooking.service;

import com.specialistbooking.dto.request.AppointmentRequest;
import com.specialistbooking.dto.request.AppointmentReportRequest;
import com.specialistbooking.entity.Appointment;
import com.specialistbooking.entity.AppointmentReport;
import com.specialistbooking.enums.AppointmentStatus;
import java.util.List;

public interface AppointmentService {
    Appointment bookAppointment(AppointmentRequest request);
    Appointment updateStatus(Long appointmentId, AppointmentStatus status);
    AppointmentReport writeReport(Long appointmentId, AppointmentReportRequest request);
    List<Appointment> getAppointmentsByDoctor(Long doctorId);
    List<Appointment> getAppointmentsByUser(Long userId);
}
```

---

### 4.11 Service Implementations — `serviceImpl/`

**LocationServiceImpl.java**
```java
package com.specialistbooking.serviceImpl;

import com.specialistbooking.entity.Location;
import com.specialistbooking.repository.LocationRepository;
import com.specialistbooking.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    public Location createLocation(Location location) {
        return locationRepository.save(location);
    }

    @Override
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }
}
```

**UserServiceImpl.java**
```java
package com.specialistbooking.serviceImpl;

import com.specialistbooking.dto.request.UserRequest;
import com.specialistbooking.entity.Location;
import com.specialistbooking.entity.User;
import com.specialistbooking.exception.ResourceNotFoundException;
import com.specialistbooking.repository.LocationRepository;
import com.specialistbooking.repository.UserRepository;
import com.specialistbooking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    @Override
    public User registerUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setLocation(location);
        return userRepository.save(user);
    }

    @Override
    public List<User> getUsersByProvince(String province) {
        return userRepository.findByLocation_Province(province);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
```

**DoctorServiceImpl.java**
```java
package com.specialistbooking.serviceImpl;

import com.specialistbooking.dto.request.DoctorRequest;
import com.specialistbooking.entity.Doctor;
import com.specialistbooking.entity.Location;
import com.specialistbooking.entity.User;
import com.specialistbooking.exception.ResourceNotFoundException;
import com.specialistbooking.repository.DoctorRepository;
import com.specialistbooking.repository.LocationRepository;
import com.specialistbooking.repository.UserRepository;
import com.specialistbooking.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    @Override
    public Doctor addDoctor(DoctorRequest request) {
        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setEmail(request.getEmail());
        doctor.setPhone(request.getPhone());
        doctor.setSpecialty(request.getSpecialty());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setHospitalName(request.getHospitalName());
        doctor.setHospitalAddress(request.getHospitalAddress());
        doctor.setLocation(location);
        return doctorRepository.save(doctor);
    }

    @Override
    public Page<Doctor> getDoctors(int page, int size, String sortBy) {
        // Satisfies pagination + sorting requirements
        return doctorRepository.findAll(
            PageRequest.of(page, size, Sort.by(sortBy).ascending()));
    }

    @Override
    public Doctor addRegularPatient(Long doctorId, Long userId) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        doctor.getRegularPatients().add(user);
        return doctorRepository.save(doctor);
    }

    @Override
    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        return doctorRepository.findBySpecialtyIgnoreCase(specialty);
    }
}
```

**ScheduleServiceImpl.java**
```java
package com.specialistbooking.serviceImpl;

import com.specialistbooking.dto.request.ScheduleRequest;
import com.specialistbooking.entity.Doctor;
import com.specialistbooking.entity.Schedule;
import com.specialistbooking.exception.ResourceNotFoundException;
import com.specialistbooking.repository.DoctorRepository;
import com.specialistbooking.repository.ScheduleRepository;
import com.specialistbooking.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public Schedule createSchedule(ScheduleRequest request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        Schedule schedule = new Schedule();
        schedule.setDoctor(doctor);
        schedule.setDate(request.getDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setBooked(false);
        return scheduleRepository.save(schedule);
    }

    @Override
    public List<Schedule> getAvailableSlots(Long doctorId) {
        return scheduleRepository.findByDoctor_IdAndIsBookedFalse(doctorId);
    }
}
```

**AppointmentServiceImpl.java**
```java
package com.specialistbooking.serviceImpl;

import com.specialistbooking.dto.request.AppointmentRequest;
import com.specialistbooking.dto.request.AppointmentReportRequest;
import com.specialistbooking.entity.*;
import com.specialistbooking.enums.AppointmentStatus;
import com.specialistbooking.exception.ResourceNotFoundException;
import com.specialistbooking.repository.*;
import com.specialistbooking.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentReportRepository reportRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    public Appointment bookAppointment(AppointmentRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
            .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        if (schedule.isBooked()) {
            throw new RuntimeException("This slot is already booked");
        }

        schedule.setBooked(true);
        scheduleRepository.save(schedule);

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setDoctor(doctor);
        appointment.setSchedule(schedule);
        appointment.setNotes(request.getNotes());
        return appointmentRepository.save(appointment);
    }

    @Override
    public Appointment updateStatus(Long appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }

    @Override
    public AppointmentReport writeReport(Long appointmentId, AppointmentReportRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        AppointmentReport report = new AppointmentReport();
        report.setAppointment(appointment);
        report.setDiagnosis(request.getDiagnosis());
        report.setPrescription(request.getPrescription());
        report.setFollowUpDate(request.getFollowUpDate());
        report.setRemarks(request.getRemarks());
        return reportRepository.save(report);
    }

    @Override
    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctor_Id(doctorId);
    }

    @Override
    public List<Appointment> getAppointmentsByUser(Long userId) {
        return appointmentRepository.findByUser_Id(userId);
    }
}
```

---

### 4.12 Controllers — `controller/`

**LocationController.java**
```java
package com.specialistbooking.controller;

import com.specialistbooking.entity.Location;
import com.specialistbooking.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<Location> create(@RequestBody Location location) {
        return ResponseEntity.ok(locationService.createLocation(location));
    }

    @GetMapping
    public ResponseEntity<List<Location>> getAll() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }
}
```

**UserController.java**
```java
package com.specialistbooking.controller;

import com.specialistbooking.dto.request.UserRequest;
import com.specialistbooking.entity.User;
import com.specialistbooking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> register(@RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @GetMapping("/province/{province}")
    public ResponseEntity<List<User>> byProvince(@PathVariable String province) {
        return ResponseEntity.ok(userService.getUsersByProvince(province));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
```

**DoctorController.java**
```java
package com.specialistbooking.controller;

import com.specialistbooking.dto.request.DoctorRequest;
import com.specialistbooking.entity.Doctor;
import com.specialistbooking.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    public ResponseEntity<Doctor> add(@RequestBody DoctorRequest request) {
        return ResponseEntity.ok(doctorService.addDoctor(request));
    }

    @GetMapping
    public ResponseEntity<Page<Doctor>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sortBy) {
        return ResponseEntity.ok(doctorService.getDoctors(page, size, sortBy));
    }

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<Doctor>> bySpecialty(@PathVariable String specialty) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialty(specialty));
    }

    @PostMapping("/{doctorId}/patients/{userId}")
    public ResponseEntity<Doctor> addRegularPatient(
            @PathVariable Long doctorId, @PathVariable Long userId) {
        return ResponseEntity.ok(doctorService.addRegularPatient(doctorId, userId));
    }
}
```

**ScheduleController.java**
```java
package com.specialistbooking.controller;

import com.specialistbooking.dto.request.ScheduleRequest;
import com.specialistbooking.entity.Schedule;
import com.specialistbooking.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<Schedule> create(@RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.createSchedule(request));
    }

    @GetMapping("/available/{doctorId}")
    public ResponseEntity<List<Schedule>> available(@PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.getAvailableSlots(doctorId));
    }
}
```

**AppointmentController.java**
```java
package com.specialistbooking.controller;

import com.specialistbooking.dto.request.AppointmentRequest;
import com.specialistbooking.dto.request.AppointmentReportRequest;
import com.specialistbooking.entity.Appointment;
import com.specialistbooking.entity.AppointmentReport;
import com.specialistbooking.enums.AppointmentStatus;
import com.specialistbooking.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> book(@RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.bookAppointment(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Appointment> updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<AppointmentReport> writeReport(
            @PathVariable Long id,
            @RequestBody AppointmentReportRequest request) {
        return ResponseEntity.ok(appointmentService.writeReport(id, request));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> byDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Appointment>> byUser(@PathVariable Long userId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByUser(userId));
    }
}
```

---

### 4.13 Request DTOs — `dto/request/`

**UserRequest.java**
```java
package com.specialistbooking.dto.request;

import lombok.Data;

@Data
public class UserRequest {
    private String name;
    private String email;
    private String phone;
    private String address;
    private Long locationId;
}
```

**DoctorRequest.java**
```java
package com.specialistbooking.dto.request;

import lombok.Data;

@Data
public class DoctorRequest {
    private String name;
    private String email;
    private String phone;
    private String specialty;
    private int experienceYears;
    private String hospitalName;
    private String hospitalAddress;
    private Long locationId;
}
```

**ScheduleRequest.java**
```java
package com.specialistbooking.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleRequest {
    private Long doctorId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
```

**AppointmentRequest.java**
```java
package com.specialistbooking.dto.request;

import lombok.Data;

@Data
public class AppointmentRequest {
    private Long userId;
    private Long doctorId;
    private Long scheduleId;
    private String notes;
}
```

**AppointmentReportRequest.java**
```java
package com.specialistbooking.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AppointmentReportRequest {
    private String diagnosis;
    private String prescription;
    private LocalDate followUpDate;
    private String remarks;
}
```

---

## PART 5 — POSTMAN TESTING

> Test in this exact order. Each step depends on data from previous steps.

| Step | Method | Endpoint | Sample Body | Tests |
|---|---|---|---|---|
| 1 | POST | /api/locations | `{"province":"Kigali","district":"Gasabo","sector":"Kimironko","cell":"Bibare","village":"Nyarutovu"}` | Location saving |
| 2 | POST | /api/users | `{"name":"John","email":"john@gmail.com","phone":"0781234567","locationId":1}` | User registration |
| 3 | POST | /api/users | Same email as step 2 | existsByEmail duplicate check |
| 4 | POST | /api/doctors | `{"name":"Dr. Sarah","specialty":"Cardiology","experienceYears":10,"hospitalName":"King Faisal","locationId":1}` | Doctor registration |
| 5 | POST | /api/schedules | `{"doctorId":1,"date":"2025-04-10","startTime":"09:00","endTime":"09:30"}` | Doctor creates slot |
| 6 | GET | /api/schedules/available/1 | No body | View available slots |
| 7 | POST | /api/appointments | `{"userId":1,"doctorId":1,"scheduleId":1,"notes":"Chest pain"}` | Patient books slot |
| 8 | POST | /api/appointments | Same body as step 7 | Double booking prevention |
| 9 | PATCH | /api/appointments/1/status?status=CONFIRMED | No body | Doctor confirms |
| 10 | POST | /api/appointments/1/report | `{"diagnosis":"Hypertension","prescription":"Amlodipine 5mg","followUpDate":"2025-05-01"}` | One-to-One report |
| 11 | POST | /api/doctors/1/patients/1 | No body | Many-to-Many regular patient |
| 12 | GET | /api/doctors?page=0&size=5&sortBy=name | No body | Pagination + sorting |
| 13 | GET | /api/users/province/Kigali | No body | Province query |

---

## PART 6 — FINAL CHECKLIST

### Database
- [ ] `specialist_booking` database created in pgAdmin
- [ ] All 6 tables auto-created on first run
- [ ] `doctor_patient` join table visible in pgAdmin
- [ ] `appointment_id` in `appointment_reports` is unique

### Exam Requirements
- [ ] One-to-One: Appointment → AppointmentReport (unique FK)
- [ ] One-to-Many: Location → Users, Location → Doctors, Doctor → Schedules, Doctor → Appointments, User → Appointments
- [ ] Many-to-Many: Doctor ↔ User via `doctor_patient` join table
- [ ] `existsByEmail()` prevents duplicate registration
- [ ] `findByLocation_Province()` returns filtered users
- [ ] `PageRequest.of(page, size, Sort.by(sortBy))` in DoctorServiceImpl
- [ ] Location table has province, district, sector, cell, village

### Testing
- [ ] All 13 Postman tests pass
- [ ] Duplicate email returns error
- [ ] Double booking same slot returns error
- [ ] `isBooked` flips to true after booking
- [ ] Appointment status changes to CONFIRMED
- [ ] Report cannot be created twice for same appointment

### Submission
- [ ] ERD diagram exported with all FKs and cardinality labels
- [ ] `.env` is in `.gitignore`
- [ ] Project starts cleanly with `./mvnw spring-boot:run`
- [ ] No hardcoded credentials anywhere in code