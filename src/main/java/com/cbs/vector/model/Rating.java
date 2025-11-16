package com.cbs.vector.model;

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
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID driverId;

    @Column(nullable = false)
    private Integer rating;

    @Lob
    private String comments;

    @NotNull
    private UUID rideId;
}