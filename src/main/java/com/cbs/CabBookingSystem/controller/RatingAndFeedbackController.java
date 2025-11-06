package com.cbs.CabBookingSystem.controller;

import com.cbs.CabBookingSystem.dto.RatingDTO;
import com.cbs.CabBookingSystem.service.RatingService;
import lombok.Getter;
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

//    @GetMapping
//    public String sayHello() {
//        return "Hello";
//    }
    // POST /api/ratings - Used for submitting a new rating and/or feedback (comments)
    @PostMapping
    public ResponseEntity<RatingDTO> createRating(@Valid @RequestBody RatingDTO ratingDTO) {
        // The @Valid annotation triggers validation rules defined in RatingDTO
        RatingDTO createdRating = ratingService.createRating(ratingDTO);
        return new ResponseEntity<>(createdRating, HttpStatus.CREATED);
    }

    // GET /api/ratings/user/{userId} - Used for fetching all ratings given by a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RatingDTO>> getRatingsByUserId(@PathVariable UUID userId) {
        List<RatingDTO> ratings = ratingService.getRatingsByUserId(userId);

        if (ratings.isEmpty()) {
            return ResponseEntity.noContent().build(); // HTTP 204 No Content
        }
        return ResponseEntity.ok(ratings); // HTTP 200 OK
    }
    // GET /api/ratings/user/{userId}/avgRating - Used for fetching all  average ratings of a specific user
    @GetMapping("/user/{userId}/avgRating")
    public ResponseEntity<Double> getAverageRating(@PathVariable UUID userId) {
        // Use the RatingService to get the calculated average
        Double avgRating = ratingService.getUserAverageRating(userId);
        return ResponseEntity.ok(avgRating);
    }
}