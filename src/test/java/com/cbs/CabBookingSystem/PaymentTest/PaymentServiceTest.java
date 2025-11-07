//package com.cbs.CabBookingSystem.PaymentTest;
//
//import com.cbs.CabBookingSystem.dto.PaymentDto;
//import com.cbs.CabBookingSystem.model.Payment;
//import com.cbs.CabBookingSystem.repository.PaymentRepository;
//import com.cbs.CabBookingSystem.service.PaymentService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class PaymentServiceTest {
//
//    @Mock
//    private PaymentRepository paymentRepository;
//
//    @InjectMocks
//    private PaymentService paymentService;
//
//    private PaymentDto paymentDto;
//    private Payment payment;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        UUID rideId = UUID.randomUUID();
//        UUID userId = UUID.randomUUID();
//
//        paymentDto = new PaymentDto();
//        paymentDto.setRideID(rideId);
//        paymentDto.setUserID(userId);
//        paymentDto.setAmount(100.0);
//        paymentDto.setMethod("PayNow");
//        paymentDto.setPickupLocation("A");
//        paymentDto.setDropLocation("B");
//
//        payment = new Payment();
//        payment.setPaymentID(1L);
//        payment.setRideID(rideId);
//        payment.setUserID(userId);
//        payment.setAmount(100.0);
//        payment.setMethod("PayNow");
//        payment.setPickupLocation("A");
//        payment.setDropLocation("B");
//        payment.setStatus("COMPLETED");
//        payment.setTimestamp(LocalDateTime.now());
//    }
//
//    @Test
//    void testProcessNewPayment_Success() {
//        when(paymentRepository.existsByRideID(paymentDto.getRideID())).thenReturn(false);
//        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
//
//        PaymentDto result = paymentService.processNewPayment(paymentDto);
//
//        assertNotNull(result);
//        assertEquals(payment.getPaymentID(), result.getPaymentID());
//        assertEquals(payment.getStatus(), result.getStatus());
//        verify(paymentRepository, times(1)).save(any(Payment.class));
//    }
//
//    @Test
//    void testProcessNewPayment_DuplicatePayment() {
//        when(paymentRepository.existsByRideID(paymentDto.getRideID())).thenReturn(true);
//
//        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
//                paymentService.processNewPayment(paymentDto)
//        );
//        assertTrue(exception.getMessage().contains("Payment already processed"));
//        verify(paymentRepository, never()).save(any(Payment.class));
//    }
//
//    @Test
//    void testGetReceiptByPaymentId_Found() {
//        when(paymentRepository.findByPaymentID(1L)).thenReturn(Optional.of(payment));
//
//        Optional<Payment> result = paymentService.getReceiptByPaymentId(1L);
//
//        assertTrue(result.isPresent());
//        assertEquals(payment.getPaymentID(), result.get().getPaymentID());
//    }
//
//    @Test
//    void testGetReceiptByPaymentId_NotFound() {
//        when(paymentRepository.findByPaymentID(2L)).thenReturn(Optional.empty());
//
//        Optional<Payment> result = paymentService.getReceiptByPaymentId(2L);
//
//        assertFalse(result.isPresent());
//    }
//
//    @Test
//    void testGeneratePaymentReceipt_Success() throws IOException {
//        byte[] pdfBytes = paymentService.generatePaymentReceipt(payment);
//
//        assertNotNull(pdfBytes);
//        assertTrue(pdfBytes.length > 0);
//    }
//}
