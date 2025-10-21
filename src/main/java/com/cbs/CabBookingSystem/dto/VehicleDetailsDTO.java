package com.cbs.CabBookingSystem.dto;

import lombok.Data;

@Data
public class VehicleDetailsDTO {
    private String vehicleNumber;
    private String vehicleMake;
    private String vehicleModel;
    private Integer vehicleYear;
    private String vehicleColor;
}