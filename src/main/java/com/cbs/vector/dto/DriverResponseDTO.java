package com.cbs.vector.dto;

import com.cbs.vector.model.DriverRole;
import com.cbs.vector.model.DriverStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DriverResponseDTO {
    private UUID id;
    private DriverRole role;
    private DriverStatus status;
    private String name; // Derived field
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Include nested DTOs, but use a response-safe version of PersonalDetails
    private PersonalDetailsResponseDTO personalDetails;
    private DriverDetailsDTO driverDetails;
    private VehicleDetailsDTO vehicleDetails;
    private BankingDetailsDTO bankingDetails;

    // Inner class for the response version of Personal Details (omits password)
    @Data
    public static class PersonalDetailsResponseDTO {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private LocalDate dateOfBirth;
    }
}