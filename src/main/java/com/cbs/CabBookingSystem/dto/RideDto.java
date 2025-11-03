package com.cbs.CabBookingSystem.dto;

import com.cbs.CabBookingSystem.model.enums.PaymentMethod;
import com.cbs.CabBookingSystem.model.enums.RideStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
//@NoArgsConstructor
@Getter
//@Setter
public class RideDto {

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
    RideStatus status;

}
