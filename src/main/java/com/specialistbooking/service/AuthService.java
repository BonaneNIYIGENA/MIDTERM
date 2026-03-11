package com.specialistbooking.service;

import com.specialistbooking.dto.request.LoginRequest;
import com.specialistbooking.dto.request.RegisterRequest;
import com.specialistbooking.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
