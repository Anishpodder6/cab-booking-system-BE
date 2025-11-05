package com.cbs.CabBookingSystem.PaymentTest;

import com.cbs.CabBookingSystem.controller.PaymentController;
import com.cbs.CabBookingSystem.dto.PaymentDto;
import com.cbs.CabBookingSystem.model.Payment;
import com.cbs.CabBookingSystem.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private PaymentDto paymentDto;
    private Payment payment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UUID rideId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        paymentDto = new PaymentDto();
        paymentDto.setRideID(rideId);
        paymentDto.setUserID(userId);
        paymentDto.setAmount(100.0);
        paymentDto.setMethod("PayNow");
        paymentDto.setPickupLocation("A");
        paymentDto.setDropLocation("B");
        paymentDto.setStatus("COMPLETED");
        paymentDto.setPaymentID(1L);
        paymentDto.setTimestamp(LocalDateTime.now());

        payment = new Payment();
        payment.setPaymentID(1L);
        payment.setRideID(rideId);
        payment.setUserID(userId);
        payment.setAmount(100.0);
        payment.setMethod("PayNow");
        payment.setPickupLocation("A");
        payment.setDropLocation("B");
        payment.setStatus("COMPLETED");
        payment.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testProcessPayment_Success() {
        when(paymentService.processNewPayment(any(PaymentDto.class))).thenReturn(paymentDto);

        ResponseEntity<?> response = paymentController.processPayment(paymentDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof PaymentDto);
        verify(paymentService, times(1)).processNewPayment(any(PaymentDto.class));
    }

    @Test
    void testProcessPayment_DuplicatePayment() {
        when(paymentService.processNewPayment(any(PaymentDto.class)))
                .thenThrow(new IllegalStateException("Payment already processed"));

        ResponseEntity<?> response = paymentController.processPayment(paymentDto);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Payment already processed"));
        verify(paymentService, times(1)).processNewPayment(any(PaymentDto.class));
    }

    @Test
    void testGetReceipt_Found() throws IOException {
        when(paymentService.getReceiptByPaymentId(1L)).thenReturn(Optional.of(payment));
        when(paymentService.generatePaymentReceipt(payment)).thenReturn(new byte[]{1, 2, 3});

        ResponseEntity<?> response = paymentController.getReceipt(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof byte[]);
        byte[] pdfBytes = (byte[]) response.getBody();
        assertEquals(3, pdfBytes.length);
        verify(paymentService, times(1)).getReceiptByPaymentId(1L);
        verify(paymentService, times(1)).generatePaymentReceipt(payment);
    }

    @Test
    void testGetReceipt_NotFound() {
        when(paymentService.getReceiptByPaymentId(2L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = paymentController.getReceipt(2L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Receipt for ride ID"));
        verify(paymentService, times(1)).getReceiptByPaymentId(2L);
        try {
            verify(paymentService, never()).generatePaymentReceipt(any());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetReceipt_PdfGenerationError() throws IOException {
        when(paymentService.getReceiptByPaymentId(1L)).thenReturn(Optional.of(payment));
        when(paymentService.generatePaymentReceipt(payment)).thenThrow(new IOException("PDF error"));

        ResponseEntity<?> response = paymentController.getReceipt(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error generating PDF receipt"));
        verify(paymentService, times(1)).getReceiptByPaymentId(1L);
        verify(paymentService, times(1)).generatePaymentReceipt(payment);
    }
}
