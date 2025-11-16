package com.cbs.vector.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class VehicleDetails {
    private String vehicleNumber;
    private String vehicleMake;
    private String vehicleModel;
    private Integer vehicleYear;
    private String vehicleColor;
}