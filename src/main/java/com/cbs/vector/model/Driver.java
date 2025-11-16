package com.cbs.vector.model;

import com.cbs.vector.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "drivers")
@Data
public class Driver implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    private PersonalDetails personalDetails;

    @Embedded
    private DriverDetails driverDetails;

    @Embedded
    private VehicleDetails vehicleDetails;

    @Embedded
    private BankingDetails bankingDetails;

    private String name;
    @Enumerated(EnumType.STRING)
    private DriverRole driverRole = DriverRole.DRIVER;

    @Enumerated(EnumType.STRING)
    private DriverStatus status = DriverStatus.UNAVAILABLE;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.DRIVER;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (personalDetails != null) {
            this.name = personalDetails.getFirstName() + " " + personalDetails.getLastName();
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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
