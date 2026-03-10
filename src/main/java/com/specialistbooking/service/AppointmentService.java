package com.specialistbooking.service;

import com.specialistbooking.dto.request.AppointmentRequest;
import com.specialistbooking.dto.request.AppointmentReportRequest;
import com.specialistbooking.entity.Appointment;
import com.specialistbooking.entity.AppointmentReport;
import com.specialistbooking.enums.AppointmentStatus;
import java.util.List;

public interface AppointmentService {
    Appointment bookAppointment(AppointmentRequest request);
    Appointment getAppointmentById(Long id);
    List<Appointment> getAllAppointments();
    Appointment updateStatus(Long appointmentId, AppointmentStatus status);
    AppointmentReport writeReport(Long appointmentId, AppointmentReportRequest request);
    AppointmentReport getReport(Long appointmentId);
    AppointmentReport updateReport(Long appointmentId, AppointmentReportRequest request);
    List<Appointment> getAppointmentsByDoctor(Long doctorId);
    List<Appointment> getAppointmentsByUser(Long userId);
    void deleteAppointment(Long id);
}