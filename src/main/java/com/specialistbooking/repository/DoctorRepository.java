package com.specialistbooking.repository;

import com.specialistbooking.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Page<Doctor> findAll(Pageable pageable);
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
    List<Doctor> findByLocation_Province(String province);
}