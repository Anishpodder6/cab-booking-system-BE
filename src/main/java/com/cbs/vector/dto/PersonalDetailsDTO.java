package com.cbs.vector.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PersonalDetailsDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    // NOTE: Passwords are included here for the registration request,
    // but should be secured (hashed) in the service layer.
    private String password;
    private String confirmPassword;
}