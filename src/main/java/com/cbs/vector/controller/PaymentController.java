package com.cbs.vector.controller;

import com.cbs.vector.dto.PaymentDto; // Use the combined DTO
import com.cbs.vector.model.Payment;
import com.cbs.vector.service.PaymentService;
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

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentDto requestDto) {
        try {
            PaymentDto responseDto = paymentService.processNewPayment(requestDto);
            log.info("Payment processed successfully for Ride ID: {}", responseDto.getRideID());
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/receipt/{rideId}")
    public ResponseEntity<?> getReceipt(@PathVariable UUID rideId) {
        Optional<Payment> response = paymentService.getReceiptByRideId(rideId);

        if (response.isPresent()) {
            Payment payment = response.get();
            try {
                // Generate the PDF byte array
                byte[] pdfBytes = paymentService.generatePaymentReceipt(payment);

                // Set HTTP Headers for PDF download
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);

                // Set Content-Disposition to 'attachment' to force download, and suggest a filename
                String filename = "receipt_" + payment.getRideID() + ".pdf";
                headers.setContentDispositionFormData("attachment", filename);

                headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

                // Return the PDF byte array with headers and status OK
                return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

            } catch (IOException e) {

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error generating PDF receipt.");
            }
        } else {
            String errorMessage = "Receipt for ride ID " + rideId + " was not found.";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
    }

    @GetMapping("/receipt/email/{rideId}")
    public ResponseEntity<?> sendReceiptEmail(@PathVariable UUID rideId,
                                              @RequestParam String email) {

        // Retrieve the Payment data
        Optional<Payment> response = paymentService.getReceiptByRideId(rideId);

        if (response.isPresent()) {
            Payment payment = response.get();
            try {
                // Generate and send the PDF receipt via email
                // The service handles both PDF generation and mailing
                paymentService.sendPaymentReceiptEmail(payment, email);

                // Return success response
                String successMessage = "Receipt for ride ID " + rideId +
                        " successfully sent to " + email + ".";
                return ResponseEntity.ok(successMessage);

            } catch (IOException e) {
                // Handle PDF generation errors
                log.error("Error generating PDF for rideId {}: {}", rideId, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error generating PDF receipt.");
            } catch (jakarta.mail.MessagingException e) {
                // Handle email sending errors
                log.error("Error sending email for rideId {} to {}: {}", rideId, email, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error sending receipt email. Check server logs for details.");
            }
        } else {
            // 4. Handle Not Found case
            String errorMessage = "Payment data for ride ID " + rideId + " was not found.";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
    }
}
