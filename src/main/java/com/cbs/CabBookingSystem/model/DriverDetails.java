package com.cbs.CabBookingSystem.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.time.LocalDate;

@Embeddable
@Data
public class DriverDetails {
    private String licenseNumber;
    private LocalDate licenseExpiry; // Use LocalDate for dates
    private Integer experience; // Experience in years
    private String emergencyName;
    private String emergencyPhone;
    private String emergencyRelation;
}