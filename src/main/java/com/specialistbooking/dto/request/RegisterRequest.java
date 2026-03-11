package com.specialistbooking.dto.request;

import com.specialistbooking.enums.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String address;
    private Long locationId;
    private Role role = Role.PATIENT;
    
    // Location fields (used when locationId is not provided)
    private String province;
    private String district;
    
    // Doctor-specific fields (optional, only used when role = DOCTOR)
    private String specialty;
    private Integer experienceYears;
    private String hospitalName;
    private String hospitalAddress;
}
