package com.cbs.vector.repository;

import com.cbs.vector.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.userId = :userId")
    Double calculateAverageRatingByUserId(UUID userId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.driverId = :driverId")
    Optional<Double> findAverageRatingByDriverId(@Param("driverId") UUID driverId);


    List<Rating> findByUserId(UUID userId);

    Optional<Rating> findByRideId(UUID rideId);
}