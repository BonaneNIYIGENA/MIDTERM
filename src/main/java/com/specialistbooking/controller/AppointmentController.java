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

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAll() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> byDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Appointment>> byUser(@PathVariable Long userId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByUser(userId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Appointment> updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<AppointmentReport> writeReport(
            @PathVariable Long id,
            @RequestBody AppointmentReportRequest request) {
        return ResponseEntity.ok(appointmentService.writeReport(id, request));
    }

    @GetMapping("/{appointmentId}/report")
    public ResponseEntity<AppointmentReport> getReport(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.getReport(appointmentId));
    }

    @PutMapping("/{appointmentId}/report")
    public ResponseEntity<AppointmentReport> updateReport(
            @PathVariable Long appointmentId,
            @RequestBody AppointmentReportRequest request) {
        return ResponseEntity.ok(appointmentService.updateReport(appointmentId, request));
    }
}