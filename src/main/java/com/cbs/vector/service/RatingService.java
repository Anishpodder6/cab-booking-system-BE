package com.cbs.vector.service;

import com.cbs.vector.dto.RatingDTO;
import com.cbs.vector.model.Rating;
import com.cbs.vector.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final RatingRepository ratingRepository;

    // Logic for POST /api/ratings
    public RatingDTO createRating(RatingDTO ratingDTO) {
        log.info("Attempting to create new rating for ride ID: {}", ratingDTO.getRideId());
        log.debug("Rating details: User ID={}, Driver ID={}, Rating={}",
                ratingDTO.getUserId(), ratingDTO.getDriverId(), ratingDTO.getRating());

        // 1. Convert DTO to Model
        Rating ratingModel = convertToModel(ratingDTO);

        UUID randomUUID = UUID.randomUUID();
        ratingModel.setId(randomUUID);

        // 2. Save Model to Database
        Rating savedModel = ratingRepository.save(ratingModel);
        log.info("Rating successfully saved with ID: {}", randomUUID);

        // 3. Convert saved Model back to DTO and return
        return convertToDTO(savedModel);
    }

    // Logic for GET /api/ratings/user/{userId}
    public List<RatingDTO> getRatingsByUserId(UUID userId) {
        log.info("Fetching all ratings for user ID: {}", userId);
        List<Rating> ratings = ratingRepository.findByUserId(userId);

        // Convert list of Models to list of DTOs
        List<RatingDTO> ratingDTOs = ratings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        log.info("Found {} ratings for user ID: {}", ratingDTOs.size(), userId);
        return ratingDTOs;
    }

    public Rating getRatingByRideId(UUID rideId) {
        log.info("Attempting to get rating by ride ID: {}", rideId);
        Rating rating = ratingRepository.findByRideId(rideId).orElse(null);
        if (rating != null) {
            log.debug("Found rating for ride ID: {}", rideId);
        } else {
            log.debug("No rating found for ride ID: {}", rideId);
        }
        return rating;
    }

    // == Helper methods for DTO <-> Model Conversion ==

    private Rating convertToModel(RatingDTO dto) {
        Rating model = new Rating();
        model.setUserId(dto.getUserId());
        model.setDriverId(dto.getDriverId());
        model.setRating(dto.getRating());
        model.setComments(dto.getComments());
        model.setRideId(dto.getRideId());
        log.debug("Converted RatingDTO to Model for ride ID: {}", dto.getRideId());
        return model;
    }

    private RatingDTO convertToDTO(Rating model) {
        RatingDTO dto = new RatingDTO();
        dto.setUserId(model.getUserId());
        dto.setDriverId(model.getDriverId());
        dto.setRating(model.getRating());
        dto.setComments(model.getComments());
        dto.setRideId(model.getRideId());
        log.debug("Converted Rating Model to DTO for ID: {}", model.getId());
        return dto;
    }

    public Double getUserAverageRating(UUID userId) {
        log.info("Calculating average rating for user ID: {}", userId);
        // Use the repository method to calculate the average
        Double avgRating = ratingRepository.calculateAverageRatingByUserId(userId);

        // Handle case where a user has no ratings yet (AVG returns null)
        Double result = (avgRating != null) ? avgRating : 0.0;
        log.info("Average rating for user {} is: {}", userId, result);
        return result;
    }
}