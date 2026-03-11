package com.specialistbooking.serviceImpl;

import com.specialistbooking.dto.request.LoginRequest;
import com.specialistbooking.dto.request.RegisterRequest;
import com.specialistbooking.dto.response.AuthResponse;
import com.specialistbooking.entity.Doctor;
import com.specialistbooking.entity.Location;
import com.specialistbooking.entity.User;
import com.specialistbooking.enums.Role;
import com.specialistbooking.exception.ResourceNotFoundException;
import com.specialistbooking.repository.DoctorRepository;
import com.specialistbooking.repository.LocationRepository;
import com.specialistbooking.repository.UserRepository;
import com.specialistbooking.security.JwtService;
import com.specialistbooking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final LocationRepository locationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.PATIENT;

        // Get location - either by ID or by province/district
        Location location = null;
        if (request.getLocationId() != null) {
            location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        } else if (request.getProvince() != null && request.getDistrict() != null) {
            // Try to find existing location by province and district
            location = locationRepository.findFirstByProvinceAndDistrict(
                    request.getProvince(), request.getDistrict()).orElse(null);
            
            // If not found, create a basic location
            if (location == null) {
                location = new Location();
                location.setProvince(request.getProvince());
                location.setDistrict(request.getDistrict());
                location.setSector("Default");
                location.setCell("Default");
                location.setVillage("Default");
                location = locationRepository.save(location);
            }
        }

        // Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setLocation(location);
        user.setRole(role);

        user = userRepository.save(user);

        // If registering as a doctor, create doctor profile
        Long doctorId = null;
        if (user.getRole() == Role.DOCTOR) {
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setName(user.getName());
            doctor.setEmail(user.getEmail());
            doctor.setPhone(user.getPhone());
            doctor.setSpecialty(request.getSpecialty() != null ? request.getSpecialty() : "General");
            doctor.setExperienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : 0);
            doctor.setHospitalName(request.getHospitalName());
            doctor.setHospitalAddress(request.getHospitalAddress());
            doctor.setLocation(location);

            doctor = doctorRepository.save(doctor);
            doctorId = doctor.getId();
        }

        // Generate JWT token
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .doctorId(doctorId)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user);

        // Get doctor ID if user is a doctor
        Long doctorId = null;
        if (user.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUser_Id(user.getId()).orElse(null);
            if (doctor != null) {
                doctorId = doctor.getId();
            }
        }

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .doctorId(doctorId)
                .build();
    }
}
