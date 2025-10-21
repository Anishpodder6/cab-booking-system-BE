package com.cbs.CabBookingSystem.dto;

import lombok.Data;

@Data
public class DriverRegistrationDTO {
    // These match the nested structure of your incoming JSON request
    private PersonalDetailsDTO personalDetails;
    private DriverDetailsDTO driverDetails;
    private VehicleDetailsDTO vehicleDetails;
    private BankingDetailsDTO bankingDetails;
}