package com.specialistbooking.repository;

import com.specialistbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    // Satisfies existBy() requirement
    boolean existsByEmail(String email);

    // Satisfies query by province requirement
    List<User> findByLocation_Province(String province);
}