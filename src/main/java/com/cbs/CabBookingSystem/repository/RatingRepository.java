package com.cbs.CabBookingSystem.repository;

import com.cbs.CabBookingSystem.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    // Custom query to calculate the average rating for a specific userId
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.userId = :userId")
    Double calculateAverageRatingByUserId(UUID userId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.driverId = :driverId")
    Optional<Double> findAverageRatingByDriverId(@Param("driverId") UUID driverId);

    // Optional: Counted total ratings a user has given for totalRides/totalRatings
    //    Long countByUserId(Long userId);

    List<Rating> findByUserId(UUID userId);

    Optional<Rating> findByRideId(UUID rideId);
}