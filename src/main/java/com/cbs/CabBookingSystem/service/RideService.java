package com.cbs.CabBookingSystem.service;

import com.cbs.CabBookingSystem.dto.RideDto;
import com.cbs.CabBookingSystem.model.Ride;
import com.cbs.CabBookingSystem.model.enums.RideStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RideService {
    Ride addRide(RideDto rideDto);
    Ride getRideById(Long rideId);
    Ride updateRideStatus(Long rideId, String status);
    Boolean deleteRide(Long rideId);
    Ride patchRideData(Map<String, Object> mp, Long rideId);
    Ride updateRideData(RideDto rideDto, Long rideId);

    Boolean assignDriver(Long rideId, Map<String, Long> mp);

    List<Ride> getRiderUpcomingRide(UUID userId);

    List<Ride> getUnassignedRides();

    List<Ride> getDriverUpcomingRide(UUID userId);

    List<Ride> getAllRidesForUser(UUID userId);
    List<Ride> getAllRides();

    Boolean hasTwoRides(UUID userId);

    RideStatus getRideStatus(Long rideId);
}
