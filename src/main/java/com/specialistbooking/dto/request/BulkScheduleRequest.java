package com.specialistbooking.dto.request;

import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
public class BulkScheduleRequest {
    private Long doctorId;
    
    // Slot duration in minutes (e.g., 30, 60)
    private Integer slotDurationMinutes = 30;
    
    // Option 1: Recurring pattern
    private RecurringPattern recurringPattern;
    
    // Option 2: Manual specific dates
    private List<ManualSchedule> manualSchedules;
    
    @Data
    public static class RecurringPattern {
        private Set<DayOfWeek> daysOfWeek;  // e.g., [MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY]
        private LocalTime startTime;         // e.g., 08:00
        private LocalTime endTime;           // e.g., 17:00
        private LocalDate fromDate;          // Start date for the pattern
        private LocalDate toDate;            // End date for the pattern (generate slots for this range)
    }
    
    @Data
    public static class ManualSchedule {
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
