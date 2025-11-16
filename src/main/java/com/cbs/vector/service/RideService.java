package com.cbs.vector.service;

import com.cbs.vector.dto.RideDto;
import com.cbs.vector.model.Ride;
import com.cbs.vector.model.RideWithRating;
import com.cbs.vector.model.enums.RideStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RideService {
    Ride addRide(RideDto rideDto);
    Ride getRideById(UUID rideId);
    Ride patchRideData(Map<String, Object> mp, UUID rideId);
    List<Ride> getRiderUpcomingRide(UUID userId);

    List<Ride> getUnassignedRides();

    List<Ride> getDriverUpcomingRide(UUID userId);

    List<Ride> getAllRidesForUser(UUID userId);

    Boolean hasTwoRides(UUID userId);

    RideStatus getRideStatus(UUID rideId);

    List<RideWithRating> getRiderHistory(UUID riderId);

    List<RideWithRating> getDriverHistory(UUID driverId);

    Map<String, Long> getCarTypeRideCountForRider(UUID userId);
}
