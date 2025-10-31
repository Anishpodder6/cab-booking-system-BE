package com.cbs.CabBookingSystem.dto;

import com.cbs.CabBookingSystem.model.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RiderRegistrationResponseDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private String role;

    public RiderRegistrationResponseDTO(User rider) {
        this.userId = rider.getUserId();
        this.firstName = rider.getFirstName();
        this.lastName = rider.getLastName();
        this.email = rider.getEmail();
        this.phone = rider.getPhone();
        this.createdAt = rider.getCreatedAt();
        this.role = rider.getRole().name();
    }

    public RiderRegistrationResponseDTO() {

    }
}
