package com.cbs.CabBookingSystem.model;



import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import java.time.LocalDate;

@Data
@Embeddable
public class PersonalDetails {
    private String firstName;
    private String lastName;

    // Add unique constraint here!
    @Column(unique = true)
    private String email;

    private String phone;
    private LocalDate dateOfBirth;
    private String password;
    private String confirmPassword;
}