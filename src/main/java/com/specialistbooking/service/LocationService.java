package com.specialistbooking.service;

import com.specialistbooking.entity.Location;
import java.util.List;

public interface LocationService {
    Location createLocation(Location location);
    List<Location> getAllLocations();
    Location getLocationById(Long id);
    List<Location> getLocationsByProvince(String province);
    Location updateLocation(Long id, Location location);
    void deleteLocation(Long id);
}