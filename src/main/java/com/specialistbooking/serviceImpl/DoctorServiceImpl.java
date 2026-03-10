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

    @Override
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    @Override
    public Doctor updateDoctor(Long id, DoctorRequest request) {
        Doctor doctor = getDoctorById(id);
        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        
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
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctorRepository.delete(doctor);
    }
}