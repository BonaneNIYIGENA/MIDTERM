package com.specialistbooking.serviceImpl;

import com.specialistbooking.dto.request.BulkScheduleRequest;
import com.specialistbooking.dto.request.ScheduleRequest;
import com.specialistbooking.entity.Doctor;
import com.specialistbooking.entity.Schedule;
import com.specialistbooking.exception.ResourceNotFoundException;
import com.specialistbooking.repository.DoctorRepository;
import com.specialistbooking.repository.ScheduleRepository;
import com.specialistbooking.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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
    @Transactional
    public List<Schedule> createBulkSchedules(BulkScheduleRequest request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        List<Schedule> createdSchedules = new ArrayList<>();
        int slotDuration = request.getSlotDurationMinutes() != null ? request.getSlotDurationMinutes() : 30;

        // Option 1: Recurring pattern
        if (request.getRecurringPattern() != null) {
            BulkScheduleRequest.RecurringPattern pattern = request.getRecurringPattern();
            LocalDate currentDate = pattern.getFromDate();
            
            while (!currentDate.isAfter(pattern.getToDate())) {
                // Check if this day matches one of the selected days of week
                if (pattern.getDaysOfWeek().contains(currentDate.getDayOfWeek())) {
                    // Generate time slots for this day
                    List<Schedule> daySlots = generateTimeSlots(
                        doctor, currentDate, 
                        pattern.getStartTime(), pattern.getEndTime(), 
                        slotDuration
                    );
                    createdSchedules.addAll(daySlots);
                }
                currentDate = currentDate.plusDays(1);
            }
        }

        // Option 2: Manual specific dates
        if (request.getManualSchedules() != null) {
            for (BulkScheduleRequest.ManualSchedule manual : request.getManualSchedules()) {
                List<Schedule> daySlots = generateTimeSlots(
                    doctor, manual.getDate(),
                    manual.getStartTime(), manual.getEndTime(),
                    slotDuration
                );
                createdSchedules.addAll(daySlots);
            }
        }

        return scheduleRepository.saveAll(createdSchedules);
    }

    private List<Schedule> generateTimeSlots(Doctor doctor, LocalDate date, 
                                              LocalTime startTime, LocalTime endTime, 
                                              int slotDurationMinutes) {
        List<Schedule> slots = new ArrayList<>();
        LocalTime currentTime = startTime;
        
        while (currentTime.plusMinutes(slotDurationMinutes).compareTo(endTime) <= 0) {
            Schedule schedule = new Schedule();
            schedule.setDoctor(doctor);
            schedule.setDate(date);
            schedule.setStartTime(currentTime);
            schedule.setEndTime(currentTime.plusMinutes(slotDurationMinutes));
            schedule.setBooked(false);
            slots.add(schedule);
            
            currentTime = currentTime.plusMinutes(slotDurationMinutes);
        }
        
        return slots;
    }

    @Override
    public List<Schedule> getAvailableSlots(Long doctorId) {
        return scheduleRepository.findByDoctor_IdAndIsBookedFalse(doctorId);
    }

    @Override
    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
    }

    @Override
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Override
    public List<Schedule> getSchedulesByDoctor(Long doctorId) {
        return scheduleRepository.findByDoctor_Id(doctorId);
    }

    @Override
    public Schedule updateSchedule(Long id, ScheduleRequest request) {
        Schedule schedule = getScheduleById(id);
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        
        schedule.setDoctor(doctor);
        schedule.setDate(request.getDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        return scheduleRepository.save(schedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        Schedule schedule = getScheduleById(id);
        scheduleRepository.delete(schedule);
    }
}