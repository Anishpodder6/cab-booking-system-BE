package com.cbs.vector.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.time.LocalDate;

@Embeddable
@Data
public class DriverDetails {
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private Integer experience;
    private String emergencyName;
    private String emergencyPhone;
    private String emergencyRelation;
}