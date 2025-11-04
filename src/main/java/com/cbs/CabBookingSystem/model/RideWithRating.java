package com.cbs.CabBookingSystem.model;

import com.cbs.CabBookingSystem.model.enums.RideStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class RideWithRating {
    UUID rideId;

    UUID userId;
    String pickupLocation;

    String dropLocation;

    UUID driverId;
    String carType;

    Double fare;

    RideStatus status = RideStatus.LookingForDriver;

    private LocalDateTime dateTime;

    Rating rating;

}
