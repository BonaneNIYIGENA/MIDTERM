package com.specialistbooking.service;

import com.specialistbooking.entity.Location;
import java.util.List;

public interface LocationService {
    Location createLocation(Location location);
    List<Location> getAllLocations();
}