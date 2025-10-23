package com.cbs.CabBookingSystem.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class RatingDTO {

//    private Long id;

    @NotNull(message = "User ID must be provided")
    private Long userId;

    @NotNull(message = "Driver ID must be provided")
    private Long driverId;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @NotNull(message = "Rating must be provided")
    private Integer rating;

    private String comments;

    private Long rideId; // Optional: Link to the specific ride
}