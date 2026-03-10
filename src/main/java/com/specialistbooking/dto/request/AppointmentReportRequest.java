package com.specialistbooking.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AppointmentReportRequest {
    private String diagnosis;
    private String prescription;
    private LocalDate followUpDate;
    private String remarks;
}