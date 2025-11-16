package com.cbs.vector.dto;

import com.cbs.vector.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequestDTO(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String passwordHash,

        @NotNull
        UserRole userRole
) { }
