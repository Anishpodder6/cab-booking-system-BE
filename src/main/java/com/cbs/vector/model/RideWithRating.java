package com.cbs.vector.model;

import com.cbs.vector.model.enums.RideStatus;
import lombok.Getter;
import lombok.Setter;

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
