package com.cbs.vector.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentID;

    private UUID rideID;
    private UUID userID;
    private String pickupLocation;
    private String dropLocation;
    private Double amount;
    private String method;
    private String status;
    private LocalDateTime timestamp;
    private String receipientEmail;

    public Payment() {
        this.timestamp = LocalDateTime.now();
    }
}