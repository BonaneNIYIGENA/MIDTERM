package com.specialistbooking.dto.request;

import lombok.Data;

@Data
public class DoctorRequest {
    private String name;
    private String email;
    private String phone;
    private String specialty;
    private int experienceYears;
    private String hospitalName;
    private String hospitalAddress;
    private Long locationId;
}