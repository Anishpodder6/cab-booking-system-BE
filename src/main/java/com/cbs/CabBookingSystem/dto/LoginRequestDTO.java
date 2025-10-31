package com.cbs.CabBookingSystem.dto;

import com.cbs.CabBookingSystem.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public record LoginRequestDTO(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String passwordHash,

        @NotNull
        UserRole userRole
) { }
