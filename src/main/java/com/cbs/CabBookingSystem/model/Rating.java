package com.cbs.CabBookingSystem.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rating")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long driverId;

    @Column(nullable = false)
    private Integer rating;

    @Lob
    private String comments;

    // Links the rating back to the specific ride
    private Long rideId;
}