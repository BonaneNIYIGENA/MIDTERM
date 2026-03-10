package com.specialistbooking.dto.request;

import lombok.Data;

@Data
public class UserRequest {
    private String name;
    private String email;
    private String phone;
    private String address;
    private Long locationId;
}