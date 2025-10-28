
package com.cbs.CabBookingSystem.repository;
//import com.cbs.CabBookingSystem.model.User;
import com.cbs.CabBookingSystem.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRideID(Long rideID);
    boolean existsByRideID(Long rideID);
}