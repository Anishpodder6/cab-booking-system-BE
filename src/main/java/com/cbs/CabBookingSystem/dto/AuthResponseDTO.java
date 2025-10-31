package com.cbs.CabBookingSystem.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private String email;
    private String role;
}