package com.cbs.vector.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateDto {

    @NotNull(message = "First name cannot be blank")
    private String firstName;
    @NotNull(message = "Last name cannot be blank")
    private String lastName;
    @NotNull(message = "Phone number cannot be blank")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;


}
