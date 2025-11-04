package com.cbs.CabBookingSystem.service;

import com.cbs.CabBookingSystem.dto.RatingDTO;
import com.cbs.CabBookingSystem.model.Rating;
import com.cbs.CabBookingSystem.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;

    // Logic for POST /api/ratings
    public RatingDTO createRating(RatingDTO ratingDTO) {
        // 1. Convert DTO to Model
        Rating ratingModel = convertToModel(ratingDTO);

        UUID randomUUID = UUID.randomUUID();
        ratingModel.setId(randomUUID);

        // 2. Save Model to Database
        Rating savedModel = ratingRepository.save(ratingModel);

        // 3. Convert saved Model back to DTO and return
        return convertToDTO(savedModel);
    }

    // Logic for GET /api/ratings/user/{userId}
    public List<RatingDTO> getRatingsByUserId(UUID userId) {
        List<Rating> ratings = ratingRepository.findByUserId(userId);

        // Convert list of Models to list of DTOs
        return ratings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Rating getRatingByRideId(UUID rideId) {

        return ratingRepository.findByRideId(rideId).orElse(null);
    }

    // == Helper methods for DTO <-> Model Conversion ==

    private Rating convertToModel(RatingDTO dto) {
        Rating model = new Rating();
        model.setUserId(dto.getUserId());
        model.setDriverId(dto.getDriverId());
        model.setRating(dto.getRating());
        model.setComments(dto.getComments());
        model.setRideId(dto.getRideId());
        return model;
    }

    private RatingDTO convertToDTO(Rating model) {
        RatingDTO dto = new RatingDTO();
        dto.setUserId(model.getUserId());
        dto.setDriverId(model.getDriverId());
        dto.setRating(model.getRating());
        dto.setComments(model.getComments());
        dto.setRideId(model.getRideId());
        return dto;
    }
    public Double getUserAverageRating(UUID userId) {
        // Use the repository method to calculate the average
        Double avgRating = ratingRepository.calculateAverageRatingByUserId(userId);

        // Handle case where a user has no ratings yet (AVG returns null)
        return (avgRating != null) ? avgRating : 0.0;
    }
}