package com.cbs.CabBookingSystem.repository;




import com.cbs.CabBookingSystem.model.Driver;
import com.cbs.CabBookingSystem.model.DriverStatus;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    @Query("SELECT d FROM Driver d WHERE " +
            "LOWER(d.personalDetails.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.personalDetails.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.carDetails.model) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    static List<Driver> searchDriverRideHistory(String keyword) {
        return null;
    }

    List<Driver> findByStatus(DriverStatus status);

    // NEW: Custom method to find a Driver by email for login/validation purposes
    Optional<Driver> findByPersonalDetailsEmail(String email);
}