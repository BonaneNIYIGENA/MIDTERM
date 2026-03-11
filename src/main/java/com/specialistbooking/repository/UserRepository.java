package com.specialistbooking.repository;

import com.specialistbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Satisfies existBy() requirement
    boolean existsByEmail(String email);

    // Find user by email (for authentication)
    Optional<User> findByEmail(String email);

    // Satisfies query by province requirement
    List<User> findByLocation_Province(String province);
}