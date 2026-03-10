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

    @Override
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        appointmentRepository.delete(appointment);
    }

    @Override
    public AppointmentReport getReport(Long appointmentId) {
        return reportRepository.findByAppointment_Id(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
    }

    @Override
    public AppointmentReport updateReport(Long appointmentId, AppointmentReportRequest request) {
        AppointmentReport report = getReport(appointmentId);
        report.setDiagnosis(request.getDiagnosis());
        report.setPrescription(request.getPrescription());
        report.setFollowUpDate(request.getFollowUpDate());
        report.setRemarks(request.getRemarks());
        return reportRepository.save(report);
    }
}