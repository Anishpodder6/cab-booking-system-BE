package com.cbs.CabBookingSystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal; // Recommended for currency/price

@Setter
@Getter
@Entity
@Table(name = "carData")
public class CarData {

    @Id // Denotes the primary key
    private String id;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    // Use BigDecimal for precise monetary values
    @Column(name = "pricePerKm", nullable = false, precision = 5, scale = 2)
    private BigDecimal pricePerKm;

    // Default no-argument constructor (required by JPA)
    public CarData() {
    }

    // Parameterized constructor (optional, but good practice)
    public CarData(String id, String type, BigDecimal pricePerKm) {
        this.id = id;
        this.type = type;
        this.pricePerKm = pricePerKm;
    }

    // Optional: Override toString() for better logging/debugging
    @Override
    public String toString() {
        return "CarData{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", pricePerKm=" + pricePerKm +
                '}';
    }
}