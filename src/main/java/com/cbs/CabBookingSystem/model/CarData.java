package com.cbs.CabBookingSystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.math.BigDecimal; // Recommended for currency/price

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

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getPricePerKm() {
        return pricePerKm;
    }

    public void setPricePerKm(BigDecimal pricePerKm) {
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