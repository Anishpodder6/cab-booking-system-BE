package com.cbs.CabBookingSystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "rating")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    @Id
    private UUID id; // Primary Key

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID driverId;

    @Column(nullable = false)
    private Integer rating;

    @Lob
    private String comments;

    // Links the rating back to the specific ride
    @NotNull
    private UUID rideId;
}