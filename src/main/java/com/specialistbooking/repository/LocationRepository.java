package com.specialistbooking.repository;

import com.specialistbooking.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByProvince(String province);
    Optional<Location> findFirstByProvinceAndDistrict(String province, String district);
}