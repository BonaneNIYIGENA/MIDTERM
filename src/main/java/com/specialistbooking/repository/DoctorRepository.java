package com.specialistbooking.repository;

import com.specialistbooking.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Page<Doctor> findAll(Pageable pageable);
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
    List<Doctor> findByLocation_Province(String province);
    List<Doctor> findByLocation_ProvinceIgnoreCase(String province);
    List<Doctor> findByLocation_DistrictIgnoreCase(String district);
    Optional<Doctor> findByUser_Id(Long userId);
    
    // Combined search: specialty and/or location
    @Query("SELECT d FROM Doctor d WHERE " +
           "(:specialty IS NULL OR LOWER(d.specialty) = LOWER(:specialty)) AND " +
           "(:province IS NULL OR LOWER(d.location.province) = LOWER(:province))")
    List<Doctor> searchDoctors(@Param("specialty") String specialty, @Param("province") String province);
    
    // Search by name (partial match)
    List<Doctor> findByNameContainingIgnoreCase(String name);
}