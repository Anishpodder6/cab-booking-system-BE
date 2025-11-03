package com.cbs.CabBookingSystem.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AuthResponseDTO {
    private String token;
    private String email;
    private String role;
    private UUID userId;
}