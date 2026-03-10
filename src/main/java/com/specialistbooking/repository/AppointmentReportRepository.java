package com.specialistbooking.repository;

import com.specialistbooking.entity.AppointmentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppointmentReportRepository extends JpaRepository<AppointmentReport, Long> {
    Optional<AppointmentReport> findByAppointment_Id(Long appointmentId);
}