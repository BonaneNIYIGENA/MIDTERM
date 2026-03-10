package com.specialistbooking.serviceImpl;

import com.specialistbooking.dto.request.UserRequest;
import com.specialistbooking.entity.Location;
import com.specialistbooking.entity.User;
import com.specialistbooking.exception.ResourceNotFoundException;
import com.specialistbooking.repository.LocationRepository;
import com.specialistbooking.repository.UserRepository;
import com.specialistbooking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    @Override
    public User registerUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setLocation(location);
        return userRepository.save(user);
    }

    @Override
    public List<User> getUsersByProvince(String province) {
        return userRepository.findByLocation_Province(province);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}