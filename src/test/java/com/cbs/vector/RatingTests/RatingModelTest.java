package com.cbs.vector.RatingTests;

import com.cbs.vector.model.Rating;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class RatingModelTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        // Arrange
        Rating rating = new Rating();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        Integer ratingValue = 5;
        String comments = "Excellent ride!";

        // Act
        rating.setId(id);
        rating.setUserId(userId);
        rating.setDriverId(driverId);
        rating.setRating(ratingValue);
        rating.setComments(comments);
        rating.setRideId(rideId);

        // Assert
        assertNotNull(rating);
        assertEquals(id, rating.getId());
        assertEquals(userId, rating.getUserId());
        assertEquals(driverId, rating.getDriverId());
        assertEquals(ratingValue, rating.getRating());
        assertEquals(comments, rating.getComments());
        assertEquals(rideId, rating.getRideId());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        Integer ratingValue = 4;
        String comments = "Good service.";

        // Act
        Rating rating = new Rating(id, userId, driverId, ratingValue, comments, rideId);

        // Assert
        assertEquals(id, rating.getId());
        assertEquals(userId, rating.getUserId());
        assertEquals(driverId, rating.getDriverId());
        assertEquals(ratingValue, rating.getRating());
        assertEquals(comments, rating.getComments());
        assertEquals(rideId, rating.getRideId());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();

        Rating rating1 = new Rating(id, userId, driverId, 5, "Great", rideId);
        Rating rating2 = new Rating(id, userId, driverId, 5, "Great", rideId);
        Rating rating3 = new Rating(UUID.randomUUID(), userId, driverId, 5, "Great", rideId);

        // Assert
        assertEquals(rating1, rating2, "Two ratings with the same data should be equal.");
        assertEquals(rating1.hashCode(), rating2.hashCode(), "Hash codes should be the same for equal objects.");
        assertNotEquals(rating1, rating3, "Ratings with different IDs should not be equal.");
    }

    @Test
    void testToString() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        Rating rating = new Rating(id, userId, driverId, 3, "Average", rideId);

        // Act
        String ratingString = rating.toString();

        // Assert
        assertTrue(ratingString.contains("id=" + id), "toString should contain the id.");
        assertTrue(ratingString.contains("userId=" + userId), "toString should contain the userId.");
        assertTrue(ratingString.contains("rating=3"), "toString should contain the rating.");
        assertTrue(ratingString.contains("comments=Average"), "toString should contain the comments.");
    }
}
