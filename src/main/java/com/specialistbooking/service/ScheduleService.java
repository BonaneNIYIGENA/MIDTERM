package com.specialistbooking.service;

import com.specialistbooking.dto.request.ScheduleRequest;
import com.specialistbooking.entity.Schedule;
import java.util.List;

public interface ScheduleService {
    Schedule createSchedule(ScheduleRequest request);
    Schedule getScheduleById(Long id);
    List<Schedule> getAllSchedules();
    List<Schedule> getSchedulesByDoctor(Long doctorId);
    List<Schedule> getAvailableSlots(Long doctorId);
    Schedule updateSchedule(Long id, ScheduleRequest request);
    void deleteSchedule(Long id);
}