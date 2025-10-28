package com.cbs.CabBookingSystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentID;

    private Long rideID;
    private Long userID;
    private String pickupLocation;
    private String dropLocation;
    private Double amount;
    private String method;
    private String status;
    private LocalDateTime timestamp;

    public Payment() {
        this.timestamp = LocalDateTime.now();
    }
}