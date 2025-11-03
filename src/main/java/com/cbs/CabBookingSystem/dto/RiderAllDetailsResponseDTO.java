package com.cbs.CabBookingSystem.dto;

import com.cbs.CabBookingSystem.model.User;
import com.cbs.CabBookingSystem.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiderAllDetailsResponseDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserRole role = UserRole.RIDER;

    private Double todaySpent;
    private Integer todayRides;
    private Double rating;
    private Double totalSpent;
    private Integer totalRides;

    public RiderAllDetailsResponseDTO(User user) {
    }
}
