package com.specialistbooking.dto.request;

import lombok.Data;

@Data
public class AppointmentRequest {
    private Long userId;
    private Long doctorId;
    private Long scheduleId;
    private String notes;
}