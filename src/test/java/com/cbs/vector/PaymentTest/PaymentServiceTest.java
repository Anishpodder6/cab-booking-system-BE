package com.cbs.vector.PaymentTest;

import com.cbs.vector.dto.PaymentDto;
import com.cbs.vector.model.Payment;
import com.cbs.vector.repository.PaymentRepository;
import com.cbs.vector.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentDto paymentDto;
    private Payment payment;
    private UUID rideId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        rideId = UUID.randomUUID();
        userId = UUID.randomUUID();

        paymentDto = new PaymentDto();
        paymentDto.setRideID(rideId);
        paymentDto.setUserID(userId);
        paymentDto.setAmount(100.0);
        paymentDto.setMethod("PayNow");
        paymentDto.setPickupLocation("A");
        paymentDto.setDropLocation("B");

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
    void testProcessNewPayment_Success() {
        when(paymentRepository.existsByRideID(rideId)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentDto result = paymentService.processNewPayment(paymentDto);

        assertNotNull(result);
        assertEquals(payment.getPaymentID(), result.getPaymentID());
        assertEquals(payment.getRideID(), result.getRideID());
        assertEquals(payment.getUserID(), result.getUserID());
        assertEquals(payment.getAmount(), result.getAmount());
        assertEquals(payment.getMethod(), result.getMethod());
        assertEquals(payment.getPickupLocation(), result.getPickupLocation());
        assertEquals(payment.getDropLocation(), result.getDropLocation());
        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getTimestamp());
        verify(paymentRepository, times(1)).existsByRideID(rideId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testProcessNewPayment_Duplicate() {
        when(paymentRepository.existsByRideID(rideId)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                paymentService.processNewPayment(paymentDto)
        );
        assertTrue(ex.getMessage().contains("Payment already processed"));
        verify(paymentRepository, times(1)).existsByRideID(rideId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testGetReceiptByPaymentId_Found() {
        when(paymentRepository.findByRideID(rideId)).thenReturn(Optional.of(payment));

        Optional<Payment> result = paymentService.getReceiptByPaymentId(rideId);

        assertTrue(result.isPresent());
        assertEquals(payment, result.get());
        verify(paymentRepository, times(1)).findByRideID(rideId);
    }

    @Test
    void testGetReceiptByPaymentId_NotFound() {
        UUID notFoundId = UUID.randomUUID();
        when(paymentRepository.findByRideID(notFoundId)).thenReturn(Optional.empty());

        Optional<Payment> result = paymentService.getReceiptByPaymentId(notFoundId);

        assertFalse(result.isPresent());
        verify(paymentRepository, times(1)).findByRideID(notFoundId);
    }

    @Test
    void testGeneratePaymentReceipt_Success() throws IOException {
        byte[] pdf = paymentService.generatePaymentReceipt(payment);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testGeneratePaymentReceipt_Error() {
        Payment badPayment = mock(Payment.class);
        when(badPayment.getPaymentID()).thenThrow(new RuntimeException("fail"));

        assertThrows(RuntimeException.class, () -> paymentService.generatePaymentReceipt(badPayment));
    }
}
