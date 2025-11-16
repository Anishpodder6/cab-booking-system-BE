
package com.cbs.vector.repository;
//import com.cbs.CabBookingSystem.model.User;
import com.cbs.vector.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRideID(UUID rideID);
    boolean existsByRideID(UUID rideID);

    @Query("""
            SELECT SUM(p.amount) FROM Payment p
            WHERE p.userID = :userId AND p.status = 'COMPLETED'
            """)
    Double sumTotalAmountByUserId(@Param("userId") UUID userId);

    @Query("""
            SELECT SUM(p.amount) FROM Payment p
            WHERE p.userID = :userId AND p.status = 'COMPLETED' AND p.timestamp >= :startOfDay
            """)
    Double sumAmountSince(@Param("userId") UUID userId, @Param("startOfDay")LocalDateTime startOfDay);

    @Query("""
        SELECT SUM(p.amount) FROM Payment p JOIN Ride r ON p.rideID = r.rideId 
        WHERE r.driverId = :driverId 
        AND p.status = 'COMPLETED'
        AND p.timestamp >= :sinceTime
    """)
    Double sumEarningsByDriverSince(@Param("driverId") UUID driverId,
                                    @Param("sinceTime") LocalDateTime sinceTime);


    @Query("""
        SELECT SUM(p.amount) FROM Payment p JOIN Ride r ON p.rideID = r.rideId 
        WHERE r.driverId = :driverId 
        AND p.status = 'COMPLETED'
    """)
    Double sumTotalEarningsByDriver(@Param("driverId") UUID driverId);

}