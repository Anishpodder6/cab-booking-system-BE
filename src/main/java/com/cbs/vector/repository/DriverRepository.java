package com.cbs.vector.repository;




import com.cbs.vector.model.Driver;
import com.cbs.vector.model.DriverStatus;
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

    Optional<Driver> findByPersonalDetailsEmail(String email);
}