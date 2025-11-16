package com.cbs.vector.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private String email;
    private String role;
    private String userId;
}