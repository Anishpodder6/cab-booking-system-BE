package com.cbs.CabBookingSystem.repository;

import com.cbs.CabBookingSystem.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Custom query to calculate the average rating for a specific userId
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.userId = :userId")
    Double calculateAverageRatingByUserId(Long userId);

    // Optional: Count total ratings a user has given (for totalRides/totalRatings logic)
//    Long countByUserId(Long userId);

    List<Rating> findByUserId(Long userId);
}