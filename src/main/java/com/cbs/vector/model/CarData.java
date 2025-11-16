package com.cbs.vector.model;

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

    @Id
    private String id;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "pricePerKm", nullable = false, precision = 5, scale = 2)
    private BigDecimal pricePerKm;

    public CarData() {
    }

    public CarData(String id, String type, BigDecimal pricePerKm) {
        this.id = id;
        this.type = type;
        this.pricePerKm = pricePerKm;
    }

    @Override
    public String toString() {
        return "CarData{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", pricePerKm=" + pricePerKm +
                '}';
    }
}