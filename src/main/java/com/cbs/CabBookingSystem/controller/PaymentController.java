package com.cbs.CabBookingSystem.controller;

import com.cbs.CabBookingSystem.dto.PaymentDto; // Use the combined DTO
import com.cbs.CabBookingSystem.model.Payment;
import com.cbs.CabBookingSystem.service.PaymentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class PaymentController {

    private PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    /**
     * Handles payment processing.
     * Uses PaymentDto for both request body (with @Valid for validation)
     * and response body.
     */
    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentDto requestDto) {
        try {
            // The service now accepts and returns PaymentDto
            PaymentDto responseDto = paymentService.processNewPayment(requestDto);
            log.info("Payment processed successfully for Ride ID: " + responseDto.getRideID());
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            // CRITICAL FIX: Catch the IllegalStateException thrown by the service
            // and return HTTP 409 Conflict with the exception's message in the body.
            System.err.println("Duplicate Payment Attempt: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/receipt/{rideId}")
    public ResponseEntity<?> getReceipt(@PathVariable UUID rideId) {
        // 1. Retrieve the Payment data
        Optional<Payment> response = paymentService.getReceiptByPaymentId(rideId);

        if (response.isPresent()) {
            Payment payment = response.get();
            try {
                // 2. Generate the PDF byte array
                byte[] pdfBytes = paymentService.generatePaymentReceipt(payment);

                // 3. Set HTTP Headers for PDF download
                HttpHeaders headers = new HttpHeaders();

                // Set the content type to PDF
                headers.setContentType(MediaType.APPLICATION_PDF);

                // Set Content-Disposition to 'attachment' to force download, and suggest a filename
                String filename = "receipt_" + payment.getRideID() + ".pdf";
                headers.setContentDispositionFormData("attachment", filename);

                // Optional: improve client caching control
                headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

                // 4. Return the PDF byte array with headers and status OK
                return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

            } catch (IOException e) {
                // Handle PDF generation errors
                System.err.println("Error generating PDF: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error generating PDF receipt.");
            }
        } else {
            // 5. Handle Not Found case
            String errorMessage = "Receipt for ride ID " + rideId + " was not found.";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
    }
}
