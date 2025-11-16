package com.cbs.vector.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Data
public class RatingDTO {


    @NotNull(message = "User ID must be provided")
    private UUID userId;

    @NotNull(message = "Driver ID must be provided")
    private UUID driverId;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @NotNull(message = "Rating must be provided")
    private Integer rating;

    private String comments;

    @NotNull
    private UUID rideId;
}