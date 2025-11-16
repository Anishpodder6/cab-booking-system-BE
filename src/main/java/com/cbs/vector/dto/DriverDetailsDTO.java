package com.cbs.vector.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DriverDetailsDTO {
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private Integer experience;
    private String emergencyName;
    private String emergencyPhone;
    private String emergencyRelation;
}