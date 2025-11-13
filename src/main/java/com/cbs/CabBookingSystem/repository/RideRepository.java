package com.cbs.CabBookingSystem.repository;

import com.cbs.CabBookingSystem.model.Ride;
import com.cbs.CabBookingSystem.model.enums.RideStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RideRepository extends JpaRepository<Ride, UUID> {
    boolean existsByRideIdAndDriverIdIsNotNull(UUID rideId);
    List<Ride> findAllByUserId(UUID userId);
    Long countByUserId(UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE Ride r SET r.driverId = :driverId WHERE r.rideId = :rideId")
    void updateDriverIdByRideId(UUID rideId, String driverId);


    @Query("""
        SELECT r FROM Ride r 
        WHERE r.driverId IS NULL 
        AND r.status NOT IN ('CancelledByUser', 'Ongoing', 'Completed') 
        AND FUNCTION('DATE', r.dateTime) = CURRENT_DATE
        ORDER BY r.dateTime DESC
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
        ORDER BY r.dateTime DESC
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
        AND r.status IN ('ConfirmedByDriver', 'Ongoing', 'Completed')
        ORDER BY r.dateTime DESC
    """)
    List<Ride> findDriverUpcomingRides(UUID userId);

    @Query("""
        SELECT COUNT(r) FROM Ride r
        WHERE r.userId = :userId
        AND DATE(r.dateTime) = CURRENT_DATE
        AND r.status NOT IN ('Completed', 'CancelledByUser')
    """)
    Long countActiveRidesByUserId(UUID userId);

    @Query("""
        SELECT COUNT(r) FROM Ride r
        WHERE r.driverId = :driverId
        AND DATE(r.dateTime) = CURRENT_DATE
        AND r.status IN ('ConfirmedByDriver', 'Ongoing')
    """)
    Long countActiveRidesByDriverId(UUID driverId);


    @Query("SELECT r.status FROM Ride r WHERE r.rideId = :rideId")
    RideStatus findStatusByRideId(UUID rideId);

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END
        FROM Ride r
        WHERE r.driverId = :driverId
        AND r.status = 'Ongoing'
        AND DATE(r.dateTime) = CURRENT_DATE
    """)
    boolean hasPreviousOngoingRide(UUID driverId);


    @Query("""
            SELECT COUNT(r) FROM Ride r
            WHERE r.userId = :userId AND r.dateTime >= :sinceTime
            """)
    Long countRidesSince(@Param("userId") UUID userId, @Param("sinceTime") LocalDateTime sinceTime);

    //Ride History
    /**
     * Finds all Ride entities associated with a specific driver ID,
     * ordered by the creation dateTime in descending order (newest first).
     */
    List<Ride> findByDriverIdOrderByDateTimeDesc(UUID driverId);

    /**
     * Finds all Ride entities associated with a specific driver ID,
     * ordered by the creation dateTime in descending order (newest first).
     */
    List<Ride> findAllByUserIdOrderByDateTimeDesc(UUID userid);

    // 1. Total Earnings Today (using the final fare from the Payment table is better,
    // but using Ride.fare for simplicity, assuming ride.fare is paid)
    @Query("""
        SELECT SUM(r.fare) FROM Ride r
        WHERE r.driverId = :driverId 
        AND r.status = 'COMPLETED' 
        AND r.dateTime >= :startOfDay
    """)
    Double sumEarningsByDriverSince(@Param("driverId") UUID driverId,
                                    @Param("startOfDay") LocalDateTime startOfDay);

    // 2. Total Rides Today
    @Query("""
        SELECT COUNT(r) FROM Ride r
        WHERE r.driverId = :driverId 
        AND r.status = 'COMPLETED'
        AND r.dateTime >= :startOfDay
    """)
    Long countCompletedRidesByDriverSince(@Param("driverId") UUID driverId,
                                          @Param("startOfDay") LocalDateTime startOfDay);

    // 3. Total Accepted Rides (for Acceptance Rate calculation)
    Long countByDriverId(UUID driverId);

    // 4. Total Rides Assigned (Including declined, pending, completed, etc., for the denominator)
    // This is a placeholder; getting the true denominator requires logging all assignment attempts.
    // For simplicity, we assume all non-unassigned/non-cancelled rides were assigned.
    @Query("SELECT COUNT(r) FROM Ride r WHERE r.driverId = :driverId")
    Long countAssignedRidesByDriver(UUID driverId);

}