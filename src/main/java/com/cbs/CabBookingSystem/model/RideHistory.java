package com.cbs.CabBookingSystem.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ride_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key

    @Column(name = "ride_history_id", unique = true, nullable = false)
    private String ridehistoryid; // The unique ID from your original structure

    private Long rideId;
    private Long userId;
    private Long driverId;
    private String driverName;
    private Boolean reviewed;

    @Lob
    private String comments;

    private String tags;
    private Integer rating;
    private String pickup;
    @Column(name = "drop_location")
    private String drop;
    private Double fare;

    @Enumerated(EnumType.STRING)
    private RideStatus status;

    private String date;
    private String travelTime;
    private String passengerName;

    // Enum for ride status
    public enum RideStatus {
        booked, ongoing, completed, cancelled, Reviewed
    }
}