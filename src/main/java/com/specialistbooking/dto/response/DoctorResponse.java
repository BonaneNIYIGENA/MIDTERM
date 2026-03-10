package com.specialistbooking.dto.response;

import lombok.Data;

@Data
public class DoctorResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String specialty;
    private int experienceYears;
    private String hospitalName;
    private String hospitalAddress;
    private Long locationId;
}