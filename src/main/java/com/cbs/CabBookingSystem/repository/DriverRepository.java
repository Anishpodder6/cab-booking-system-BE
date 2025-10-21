package com.cbs.CabBookingSystem.repository;




import com.cbs.CabBookingSystem.model.Driver;
import com.cbs.CabBookingSystem.model.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    List<Driver> findByStatus(DriverStatus status);

    // NEW: Custom method to find a Driver by email for login/validation purposes
    List<Driver> findByPersonalDetailsEmail(String email);
}