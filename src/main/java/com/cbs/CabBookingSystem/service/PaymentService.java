package com.cbs.CabBookingSystem.service;

import com.cbs.CabBookingSystem.dto.PaymentDto; // Import the new combined DTO
import com.cbs.CabBookingSystem.model.Payment;
import com.cbs.CabBookingSystem.repository.PaymentRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    // Use PaymentDto for request and response
    public PaymentDto processNewPayment(PaymentDto requestDto) {

        if (paymentRepository.existsByRideID(requestDto.getRideID())) {
            throw new IllegalStateException("Payment already processed for ride ID: " + requestDto.getRideID());
        }

        // 1. Convert DTO to Entity
        Payment payment = new Payment();
        payment.setRideID(requestDto.getRideID());
        payment.setUserID(requestDto.getUserID());
        payment.setAmount(requestDto.getAmount());
        payment.setMethod(requestDto.getMethod());

        // FIX: The original code had getters on the payment entity,
        // which should be setters for setting values from the DTO.
        // Assuming your Payment entity has setPickupLocation and setDropLocation
        // and these fields were in your original PaymentRequestDto
        // The Payment model must have these fields and setter methods.
        // If not, you may remove these lines or update your Payment model.
        // payment.getPickupLocation(requestDto.getPickupLocation()); // Original incorrect line
        // payment.getDropLocation(requestDto.getDropLocation()); // Original incorrect line
        payment.setPickupLocation(requestDto.getPickupLocation());
        payment.setDropLocation(requestDto.getDropLocation());

        // 2. Business Logic: Set initial status and timestamp on the server side
        payment.setStatus("COMPLETED");
        payment.setTimestamp(LocalDateTime.now());

        // 3. Save to database
        Payment savedPayment = paymentRepository.save(payment);

        // 4. Convert saved Entity back to Response DTO (now PaymentDto)
        return mapToDto(savedPayment);
    }

    // Use PaymentDto for response
    public Optional<Payment> getReceiptByRideId(Long rideId) {
        return paymentRepository.findByRideID(rideId);
//                .map(this::mapToDto);
    }


    public byte[] generatePaymentReceipt(Payment payment) throws IOException {
        // Use ByteArrayOutputStream to write PDF to memory
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


        // iText specific objects: PdfWriter writes the PDF, PdfDocument wraps it
        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // --- 1. Title and Header ---
            document.add(new Paragraph("Vector - Official Receipt")
                    .setFontSize(20)
//                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Receipt ID: " + payment.getPaymentID())
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10));

            // --- 2. Ride and User Details ---
            document.add(new Paragraph("\n--- Ride Details ---").setFontSize(14));
            document.add(new Paragraph("Ride ID: " + payment.getRideID()));
            document.add(new Paragraph("User ID: " + payment.getUserID()));
            document.add(new Paragraph("Date & Time: " + payment.getTimestamp()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))));

            // --- 3. Location Details ---
            document.add(new Paragraph("\n--- Trip Route ---").setFontSize(14));
            document.add(new Paragraph("Pickup: " + payment.getPickupLocation()));
            document.add(new Paragraph("Drop-off: " + payment.getDropLocation()));

            // --- 4. Financial Summary ---
            document.add(new Paragraph("\n--- Payment Summary ---").setFontSize(14));
            document.add(new Paragraph("Amount Paid: INR " + String.format("%.2f", payment.getAmount()))
                    );
            document.add(new Paragraph("Payment Method: " + payment.getMethod()));
            document.add(new Paragraph("Status: " + payment.getStatus()));

            // --- 5. Footer ---
            document.add(new Paragraph("\n\nThank you for riding with us!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10));

        } catch (Exception e) {
            // Log error and rethrow as an IOException to be handled by the controller
            System.err.println("iText PDF Generation Error: " + e.getMessage());
            throw new IOException("Failed to generate PDF receipt.", e);
        }

        return outputStream.toByteArray();
    }

    /**
     * Helper method to map Payment entity to PaymentDto
     */
    private PaymentDto mapToDto(Payment entity) {
        // Use the new combined PaymentDto
        PaymentDto dto = new PaymentDto();
        dto.setPaymentID(entity.getPaymentID());
        dto.setRideID(entity.getRideID());
        dto.setUserID(entity.getUserID());
        dto.setAmount(entity.getAmount());
        dto.setMethod(entity.getMethod());
        dto.setStatus(entity.getStatus());
        dto.setTimestamp(entity.getTimestamp());
        // Assuming these fields exist in your Payment entity
        // If not, remove the following lines
        dto.setPickupLocation(entity.getPickupLocation());
        dto.setDropLocation(entity.getDropLocation());
        return dto;
    }
}