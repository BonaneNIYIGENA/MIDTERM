package com.specialistbooking.serviceImpl;

import com.specialistbooking.entity.Location;
import com.specialistbooking.exception.ResourceNotFoundException;
import com.specialistbooking.repository.LocationRepository;
import com.specialistbooking.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    public Location createLocation(Location location) {
        return locationRepository.save(location);
    }

    @Override
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @Override
    public Location getLocationById(Long id) {
        return locationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
    }

    @Override
    public List<Location> getLocationsByProvince(String province) {
        return locationRepository.findByProvince(province);
    }

    @Override
    public Location updateLocation(Long id, Location locationUpdate) {
        Location location = getLocationById(id);
        location.setProvince(locationUpdate.getProvince());
        location.setDistrict(locationUpdate.getDistrict());
        location.setSector(locationUpdate.getSector());
        location.setCell(locationUpdate.getCell());
        location.setVillage(locationUpdate.getVillage());
        return locationRepository.save(location);
    }

    @Override
    public void deleteLocation(Long id) {
        Location location = getLocationById(id);
        locationRepository.delete(location);
    }
}