package com.cbs.CabBookingSystem.repository;

import com.cbs.CabBookingSystem.model.Ride;
import com.cbs.CabBookingSystem.model.enums.RideStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    boolean existsByRideIdAndDriverIdIsNotNull(Long rideId);
    List<Ride> findAllByUserId(UUID userId);
    Long countByUserId(UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE Ride r SET r.driverId = :driverId WHERE r.rideId = :rideId")
    void updateDriverIdByRideId(Long rideId, Long driverId);


    @Query("""
        SELECT r FROM Ride r 
        WHERE r.driverId IS NULL 
        AND r.status NOT IN ('CancelledByUser', 'Ongoing', 'Completed') 
        AND FUNCTION('DATE', r.dateTime) = CURRENT_DATE
        ORDER BY r.dateTime ASC
    """)
    List<Ride> findUnassignedRides();

    /**
     * 2. Fetches upcoming rides for a specific rider (user).
     * Criteria: userId matches the requested userId AND rideStatus NOT IN ('CancelledByUser', 'Completed')
     */

    @Query("""
        SELECT r FROM Ride r 
        WHERE r.userId = :userId 
        AND DATE(r.dateTime) = CURRENT_DATE 
        AND r.status NOT IN ('Completed', 'CancelledByUser')
        ORDER BY r.dateTime ASC
    """)
    List<Ride> findRiderUpcomingRides(UUID userId);

    /**
     * 3. Fetches upcoming rides for a specific driver.
     * Criteria: driverId matches the requested userId AND rideStatus IN ('ConfirmedByDriver', 'Ongoing')
     */

    @Query("""
        SELECT r FROM Ride r 
        WHERE r.driverId = :userId 
        AND DATE(r.dateTime) = CURRENT_DATE 
        AND r.status IN ('ConfirmedByDriver', 'Ongoing')
        ORDER BY r.dateTime ASC
    """)
    List<Ride> findDriverUpcomingRides(UUID userId);

    @Query("""
        SELECT COUNT(r) FROM Ride r
        WHERE r.userId = :userId
        AND r.status NOT IN ('Completed', 'CancelledByUser')
    """)
    Long countActiveRidesByUserId(UUID userId);


    @Query("SELECT r.status FROM Ride r WHERE r.rideId = :rideId")
    RideStatus findStatusByRideId(Long rideId);


    @Query("""
            SELECT COUNT(r) FROM Ride r
            WHERE r.userId = :userId AND r.dateTime >= :sinceTime
            """)
    Long countRidesSince(@Param("userId") UUID userId, @Param("sinceTime") LocalDateTime sinceTime);

}