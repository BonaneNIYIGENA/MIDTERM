package com.specialistbooking.controller;

import com.specialistbooking.dto.request.BulkScheduleRequest;
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

    @PostMapping("/bulk")
    public ResponseEntity<List<Schedule>> createBulk(@RequestBody BulkScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.createBulkSchedules(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    @GetMapping
    public ResponseEntity<List<Schedule>> getAll() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Schedule>> byDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByDoctor(doctorId));
    }

    @GetMapping("/available/{doctorId}")
    public ResponseEntity<List<Schedule>> available(@PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.getAvailableSlots(doctorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Schedule> update(@PathVariable Long id, @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}