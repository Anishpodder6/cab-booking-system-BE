package com.cbs.vector.dto;

import com.cbs.vector.model.DriverStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DriverUpdateDTO {
    // Personal Details that can be updated
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate dateOfBirth;
    // Note: Email is often excluded or handled separately due to its unique constraint
    // We will allow email updates in the service layer, but it will trigger a unique check.

    // Driver Details
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private Integer experience;
    private String emergencyName;
    private String emergencyPhone;
    private String emergencyRelation;

    // Vehicle Details
    private String vehicleNumber;
    private String vehicleMake;
    private String vehicleModel;
    private Integer vehicleYear;
    private String vehicleColor;

    // Banking Details
    private String bankAccount;
    private String routingNumber;

    // Optional status update (if the driver can change this manually)
    private DriverStatus status;
}