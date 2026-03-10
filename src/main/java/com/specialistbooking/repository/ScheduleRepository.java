package com.specialistbooking.repository;

import com.specialistbooking.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByDoctor_IdAndIsBookedFalse(Long doctorId);
    List<Schedule> findByDoctor_IdAndDate(Long doctorId, LocalDate date);
    List<Schedule> findByDoctor_Id(Long doctorId);
}