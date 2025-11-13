package com.cbs.CabBookingSystem.model;

import com.cbs.CabBookingSystem.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "drivers") // Recommended to name your table in plural
@Data
public class Driver implements UserDetails {

    // Matches the "id": "ded4" format from your JSON. Uses UUID internally.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Embeddable fields will be mapped as columns in the 'drivers' table
    @Embedded
    private PersonalDetails personalDetails;

    @Embedded
    private DriverDetails driverDetails;

    @Embedded
    private VehicleDetails vehicleDetails;

    @Embedded
    private BankingDetails bankingDetails;

    // Direct columns from the root JSON
    private String name; // "name": "undefined undefined"

    // Enum for role is better practice
    @Enumerated(EnumType.STRING)
    private DriverRole driverRole = DriverRole.DRIVER; // Default to 'DRIVER'

    // New field for the PUT API: /api/drivers/status/{id}
    @Enumerated(EnumType.STRING)
    private DriverStatus status = DriverStatus.UNAVAILABLE;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.DRIVER;

    // Timestamps
//    @Temporal(TemporalType.INSTANT)
    private LocalDateTime createdAt;

//    @Temporal(TemporalType.INSTANT)
    private LocalDateTime updatedAt;

    // Use JPA Lifecycle callbacks to manage timestamps and name
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Set 'name' on creation based on personal details
        if (personalDetails != null) {
            this.name = personalDetails.getFirstName() + " " + personalDetails.getLastName();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        // Update 'name' if details are changed
        if (personalDetails != null) {
            this.name = personalDetails.getFirstName() + " " + personalDetails.getLastName();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return personalDetails.getPassword();
    }

    @Override
    public String getUsername() {
        return personalDetails.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
