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