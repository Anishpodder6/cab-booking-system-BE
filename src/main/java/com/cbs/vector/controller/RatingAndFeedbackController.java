package com.cbs.vector.controller;

import com.cbs.vector.dto.RatingDTO;
import com.cbs.vector.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Slf4j
public class RatingAndFeedbackController {

    private final RatingService ratingService;

    // POST - Used for submitting a new rating and/or feedback (comments)
    @PostMapping
    public ResponseEntity<RatingDTO> createRating(@Valid @RequestBody RatingDTO ratingDTO) {
        // The @Valid annotation triggers validation rules defined in RatingDTO
        RatingDTO createdRating = ratingService.createRating(ratingDTO);
        log.info("Request to create a new rating for ride ID: {}", ratingDTO.getRideId());
        return new ResponseEntity<>(createdRating, HttpStatus.CREATED);
    }

    // GET - Used for fetching all ratings given by a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RatingDTO>> getRatingsByUserId(@PathVariable UUID userId) {
        log.info("Request to fetch all ratings given by user ID: {}", userId);
        List<RatingDTO> ratings = ratingService.getRatingsByUserId(userId);

        if (ratings.isEmpty()) {
            log.info("No ratings found for user ID: {}", userId);
            return ResponseEntity.noContent().build(); // HTTP 204 No Content
        }
        log.info("Fetched {} ratings for user ID: {}", ratings.size(), userId);
        return ResponseEntity.ok(ratings); // HTTP 200 OK
    }
    // GET /api/ratings/user/{userId}/avgRating - Used for fetching all  average ratings of a specific user
    @GetMapping("/user/{userId}/avgRating")
    public ResponseEntity<Double> getAverageRating(@PathVariable UUID userId) {
        Double avgRating = ratingService.getUserAverageRating(userId);
        log.info("Average rating for user ID {} is: {}", userId, avgRating);
        return ResponseEntity.ok(avgRating);
    }
}