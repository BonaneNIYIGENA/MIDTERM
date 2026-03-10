package com.specialistbooking.controller;

import com.specialistbooking.dto.request.UserRequest;
import com.specialistbooking.entity.User;
import com.specialistbooking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> register(@RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @GetMapping("/province/{province}")
    public ResponseEntity<List<User>> byProvince(@PathVariable String province) {
        return ResponseEntity.ok(userService.getUsersByProvince(province));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}