package com.cbs.vector.RatingTests;

import com.cbs.vector.dto.RatingDTO;
import com.cbs.vector.model.Rating;
import com.cbs.vector.repository.RatingRepository;
import com.cbs.vector.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private RatingService ratingService;

    private UUID userId;
    private UUID driverId;
    private UUID rideId;

    @BeforeEach
    void init() {
        userId = UUID.randomUUID();
        driverId = UUID.randomUUID();
        rideId = UUID.randomUUID();
    }

    @Test
    void createRating_ShouldPersistAndReturnDTO() {
        RatingDTO dto = new RatingDTO();
        dto.setUserId(userId);
        dto.setDriverId(driverId);
        dto.setRating(5);
        dto.setComments("Great ride");
        dto.setRideId(rideId);

        // Make repository.save return the same entity it receives
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RatingDTO result = ratingService.createRating(dto);

        // Capture argument passed to save
        ArgumentCaptor<Rating> captor = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).save(captor.capture());
        Rating saved = captor.getValue();

        assertNotNull(saved.getId(), "ID should be generated before save");
        assertEquals(dto.getUserId(), result.getUserId());
        assertEquals(dto.getDriverId(), result.getDriverId());
        assertEquals(dto.getRating(), result.getRating());
        assertEquals(dto.getComments(), result.getComments());
        assertEquals(dto.getRideId(), result.getRideId());
    }

    @Test
    void getRatingsByUserId_ShouldReturnMappedDTOList() {
        Rating r1 = new Rating(UUID.randomUUID(), userId, driverId, 4, "Good", rideId);
        Rating r2 = new Rating(UUID.randomUUID(), userId, driverId, 5, "Excellent", UUID.randomUUID());
        when(ratingRepository.findByUserId(userId)).thenReturn(List.of(r1, r2));

        List<RatingDTO> list = ratingService.getRatingsByUserId(userId);

        assertEquals(2, list.size());
        assertEquals(r1.getRating(), list.get(0).getRating());
        assertEquals(r2.getComments(), list.get(1).getComments());
        verify(ratingRepository).findByUserId(userId);
    }

    @Test
    void getRatingsByUserId_WhenEmpty_ShouldReturnEmptyList() {
        when(ratingRepository.findByUserId(userId)).thenReturn(List.of());

        List<RatingDTO> list = ratingService.getRatingsByUserId(userId);

        assertNotNull(list);
        assertTrue(list.isEmpty());
        verify(ratingRepository).findByUserId(userId);
    }

    @Test
    void getRatingByRideId_WhenPresent_ShouldReturnEntity() {
        Rating rating = new Rating(UUID.randomUUID(), userId, driverId, 3, "Ok", rideId);
        when(ratingRepository.findByRideId(rideId)).thenReturn(Optional.of(rating));

        Rating result = ratingService.getRatingByRideId(rideId);

        assertNotNull(result);
        assertEquals(rating.getId(), result.getId());
        verify(ratingRepository).findByRideId(rideId);
    }

    @Test
    void getRatingByRideId_WhenMissing_ShouldReturnNull() {
        when(ratingRepository.findByRideId(rideId)).thenReturn(Optional.empty());

        Rating result = ratingService.getRatingByRideId(rideId);

        assertNull(result);
        verify(ratingRepository).findByRideId(rideId);
    }

    @Test
    void getUserAverageRating_WhenValueExists_ShouldReturnIt() {
        when(ratingRepository.calculateAverageRatingByUserId(userId)).thenReturn(4.25);

        Double avg = ratingService.getUserAverageRating(userId);

        assertEquals(4.25, avg);
        verify(ratingRepository).calculateAverageRatingByUserId(userId);
    }

    @Test
    void getUserAverageRating_WhenNoRatings_ShouldReturnZero() {
        when(ratingRepository.calculateAverageRatingByUserId(userId)).thenReturn(null);

        Double avg = ratingService.getUserAverageRating(userId);

        assertEquals(0.0, avg);
        verify(ratingRepository).calculateAverageRatingByUserId(userId);
    }
}
