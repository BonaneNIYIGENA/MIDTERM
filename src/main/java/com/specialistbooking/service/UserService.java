package com.specialistbooking.service;

import com.specialistbooking.dto.request.UserRequest;
import com.specialistbooking.entity.User;
import java.util.List;

public interface UserService {
    User registerUser(UserRequest request);
    List<User> getUsersByProvince(String province);
    User getUserById(Long id);
}