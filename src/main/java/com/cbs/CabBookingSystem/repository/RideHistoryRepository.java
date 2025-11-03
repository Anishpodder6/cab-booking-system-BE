package com.cbs.CabBookingSystem.repository;

import com.cbs.CabBookingSystem.model.Ride;
import com.cbs.CabBookingSystem.model.enums.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RideHistoryRepository extends JpaRepository<Ride, Long> {

    /**
     * Fetches rides where the rider's ID matches the given UUID and the ride is COMPLETED.
     * Used for Rider Ride History.
     * @param riderId The UUID of the User (Rider).
     * @param status The status (e.g., RideStatus.COMPLETED).
     * @return List of completed Rides.
     */
//    List<Ride> findByRider_IdAndStatusOrderByEndTimeDesc(UUID riderId, RideStatus status);
//
//    /**
//     * Fetches rides where the driver's ID matches the given UUID and the ride is COMPLETED.
//     * Used for Driver Ride History.
//     * @param driverId The UUID of the Driver.
//     * @param status The status (e.g., RideStatus.COMPLETED).
//     * @return List of completed Rides.
//     */
//    List<Ride> findByDriver_IdAndStatusOrderByEndTimeDesc(UUID driverId, RideStatus status);
}