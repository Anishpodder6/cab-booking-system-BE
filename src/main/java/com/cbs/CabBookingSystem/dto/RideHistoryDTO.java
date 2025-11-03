package com.cbs.CabBookingSystem.dto;

import com.cbs.CabBookingSystem.model.enums.RideStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RideHistoryDTO {
    private UUID rideId;
    private LocalDateTime bookingTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String pickupLocation;
    private String dropLocation;
    private Double fare;
    private RideStatus status;

    // Details about the other party (Driver for Rider history, Rider for Driver history)
    private UUID partnerId;
    private String partnerName;
    private String partnerRole; // "DRIVER" or "RIDER"

    // Placeholder for Rating Submission integration
    private boolean isRated;
}