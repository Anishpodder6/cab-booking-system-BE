package com.cbs.CabBookingSystem.model;

import com.cbs.CabBookingSystem.model.enums.PaymentMethod;
import com.cbs.CabBookingSystem.model.enums.RideStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity()
@Table(name = "ride")
public class Ride {

    @Id
    UUID rideId;

    @NotNull
    UUID userId;

    @NotBlank
    String pickupLocation;

    @NotBlank
    String dropLocation;

    UUID driverId;
    String carType;

    @NotNull
    Double fare;

    @NotNull
    @Enumerated(EnumType.STRING)
    RideStatus status = RideStatus.LookingForDriver;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateTime;

    private String receipientEmail;

}
