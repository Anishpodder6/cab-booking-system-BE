package com.cbs.vector.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class BankingDetails {
    private String bankAccount;
    private String routingNumber;
}