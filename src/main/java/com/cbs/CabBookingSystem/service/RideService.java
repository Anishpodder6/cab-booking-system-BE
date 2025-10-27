package com.cbs.CabBookingSystem.service;

import com.cbs.CabBookingSystem.dto.RideDto;
import com.cbs.CabBookingSystem.model.Ride;
import com.cbs.CabBookingSystem.model.enums.RideStatus;

import java.util.List;
import java.util.Map;

public interface RideService {
    Ride addRide(RideDto rideDto);
    Ride getRideById(Long rideId);
    Ride updateRideStatus(Long rideId, String status);
    Boolean deleteRide(Long rideId);
    Ride patchRideData(Map<String, Object> mp, Long rideId);
    Ride updateRideData(RideDto rideDto, Long rideId);

    Boolean assignDriver(Long rideId, Map<String, Long> mp);

    List<Ride> getRiderUpcomingRide(Long userId);

    List<Ride> getUnassignedRides();

    List<Ride> getDriverUpcomingRide(Long userId);

    List<Ride> getAllRidesForUser(Long userId);
    List<Ride> getAllRides();

    Boolean hasTwoRides(Long userId);

    RideStatus getRideStatus(Long rideId);
}
