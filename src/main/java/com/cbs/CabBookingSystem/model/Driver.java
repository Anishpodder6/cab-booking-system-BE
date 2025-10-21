package com.cbs.CabBookingSystem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "drivers") // Recommended to name your table in plural
@Data
public class Driver {

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
    private DriverRole role = DriverRole.DRIVER; // Default to 'DRIVER'

    // New field for the PUT API: /api/drivers/status/{id}
    @Enumerated(EnumType.STRING)
    private DriverStatus status = DriverStatus.UNAVAILABLE;

    // Timestamps
//    @Temporal(TemporalType.INSTANT)
    private Instant createdAt;

//    @Temporal(TemporalType.INSTANT)
    private Instant updatedAt;

    // Use JPA Lifecycle callbacks to manage timestamps and name
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        // Set 'name' on creation based on personal details
        if (personalDetails != null) {
            this.name = personalDetails.getFirstName() + " " + personalDetails.getLastName();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        // Update 'name' if details are changed
        if (personalDetails != null) {
            this.name = personalDetails.getFirstName() + " " + personalDetails.getLastName();
        }
    }
}
