package com.cbs.CabBookingSystem.service;

import com.cbs.CabBookingSystem.dto.RideDto;
import com.cbs.CabBookingSystem.dto.RideHistoryDTO;
import com.cbs.CabBookingSystem.model.Ride;
import com.cbs.CabBookingSystem.model.enums.RideStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RideService {
    Ride addRide(RideDto rideDto);
    Ride getRideById(UUID rideId);
    Ride updateRideStatus(UUID rideId, String status);
    Boolean deleteRide(UUID rideId);
    Ride patchRideData(Map<String, Object> mp, UUID rideId);
    Ride updateRideData(RideDto rideDto, UUID rideId);

    Boolean assignDriver(UUID rideId, Map<String, String> mp);

    List<Ride> getRiderUpcomingRide(UUID userId);

    List<Ride> getUnassignedRides();

    List<Ride> getDriverUpcomingRide(UUID userId);

    List<Ride> getAllRidesForUser(UUID userId);
    List<Ride> getAllRides();

    Boolean hasTwoRides(UUID userId);

    RideStatus getRideStatus(UUID rideId);

    List<RideHistoryDTO> getRiderHistory(UUID riderId);

    List<RideHistoryDTO> getDriverHistory(UUID driverId);
}
