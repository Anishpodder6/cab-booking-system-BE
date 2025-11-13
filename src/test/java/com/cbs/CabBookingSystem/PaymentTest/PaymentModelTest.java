package com.cbs.CabBookingSystem.PaymentTest;

import com.cbs.CabBookingSystem.model.Payment;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentModelTest {

    @Test
    void testPaymentGettersAndSetters() {
        Payment payment = new Payment();

        Long paymentID = 1L;
        UUID rideID = UUID.randomUUID();
        UUID userID = UUID.randomUUID();
        String pickupLocation = "A";
        String dropLocation = "B";
        Double amount = 100.0;
        String method = "Credit Card";
        String status = "Completed";
        LocalDateTime timestamp = LocalDateTime.now();
        String recipientEmail = "test@example.com";

        payment.setPaymentID(paymentID);
        payment.setRideID(rideID);
        payment.setUserID(userID);
        payment.setPickupLocation(pickupLocation);
        payment.setDropLocation(dropLocation);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setStatus(status);
        payment.setTimestamp(timestamp);
        payment.setRecipientEmail(recipientEmail);

        assertEquals(paymentID, payment.getPaymentID());
        assertEquals(rideID, payment.getRideID());
        assertEquals(userID, payment.getUserID());
        assertEquals(pickupLocation, payment.getPickupLocation());
        assertEquals(dropLocation, payment.getDropLocation());
        assertEquals(amount, payment.getAmount());
        assertEquals(method, payment.getMethod());
        assertEquals(status, payment.getStatus());
        assertEquals(timestamp, payment.getTimestamp());
        assertEquals(recipientEmail, payment.getRecipientEmail());
    }

    @Test
    void testDefaultConstructorInitializesTimestamp() {
        Payment payment = new Payment();
        assertNotNull(payment.getTimestamp());
        assertTrue(payment.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}
