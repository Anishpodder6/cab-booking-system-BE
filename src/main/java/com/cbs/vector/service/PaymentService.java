package com.cbs.vector.service;

import com.cbs.vector.dto.PaymentDto; // Import the new combined DTO
import com.cbs.vector.model.Payment;
import com.cbs.vector.repository.PaymentRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EmailService emailService;

    // Use PaymentDto for request and response
    public PaymentDto processNewPayment(PaymentDto requestDto) {
        log.info("Starting payment processing for Ride ID: {}", requestDto.getRideID());

        if (paymentRepository.existsByRideID(requestDto.getRideID())) {
            log.warn("Payment already processed for ride ID: {}", requestDto.getRideID());
            throw new IllegalStateException("Payment already processed for ride ID: " + requestDto.getRideID());
        }

        // 1. Convert DTO to Entity
        Payment payment = new Payment();
        payment.setRideID(requestDto.getRideID());
        payment.setUserID(requestDto.getUserID());
        payment.setAmount(requestDto.getAmount());
        payment.setMethod(requestDto.getMethod());
        payment.setPickupLocation(requestDto.getPickupLocation());
        payment.setDropLocation(requestDto.getDropLocation());

        // 2. Business Logic: Set initial status and timestamp on the server side
        payment.setStatus("COMPLETED");
        payment.setTimestamp(LocalDateTime.now());
        payment.setReceipientEmail(requestDto.getReceipientEmail());
        log.debug("Payment entity created: {}", payment);


        // 3. Save to database
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment successfully saved to DB with Payment ID: {}", savedPayment.getPaymentID());


//        try {
//            sendPaymentReceiptEmail(savedPayment, requestDto.getReceipientEmail());
//            log.info("Payment receipt email successfully sent to: {}", requestDto.getReceipientEmail());
//
//        } catch (IOException e) {
//            log.error("CRITICAL ERROR: Failed to generate PDF receipt for payment ID {}. Error: {}",
//                    savedPayment.getPaymentID(), e.getMessage(), e);
//
//        } catch (MessagingException e) {
//            log.error("CRITICAL ERROR: Failed to send email for payment ID {}. Error: {}",
//                    savedPayment.getPaymentID(), e.getMessage(), e);
//        }

        // 4. Convert saved Entity back to Response DTO (now PaymentDto)
        return mapToDto(savedPayment);

    }

    public Optional<Payment> getReceiptByPaymentId(UUID rideId) {
        log.info("Attempting to retrieve payment receipt by Ride ID: {}", rideId);
        return paymentRepository.findByRideID(rideId);
    }

    public void sendPaymentReceiptEmail(Payment payment, String recipientEmail)
            throws IOException, MessagingException {

        log.info("Preparing to send receipt for Payment ID {} to {}", payment.getPaymentID(), recipientEmail);

        // 1. Generate the PDF
        byte[] pdfBytes = generatePaymentReceipt(payment);
        log.debug("PDF receipt generated successfully ({} bytes).", pdfBytes.length);

        // 2. Prepare email details
        String filename = "receipt_" + payment.getRideID() + ".pdf";
        String subject = "Your Cab Booking System Payment Receipt for Ride " + payment.getRideID();

        String body = String.format("Dear Customer,<br><br>Thank you for your payment. " +
                        "Please find your official receipt for Ride ID <b>%s</b> attached.<br><br>" +
                        "Amount: INR %.2f<br>Date: %s<br><br>Vector Team",
                payment.getRideID(),
                payment.getAmount(),
                payment.getTimestamp().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));

        // 3. Send the email
        emailService.sendEmailWithAttachment(recipientEmail, subject, body, pdfBytes, filename);
        log.debug("Email service called for sending receipt.");
    }

    // Use PaymentDto for response
    public Optional<Payment> getReceiptByRideId(UUID rideId) {
        log.info("Attempting to retrieve payment by Ride ID: {}", rideId);
        return paymentRepository.findByRideID(rideId);
    }


    public byte[] generatePaymentReceipt(Payment payment) throws IOException {
        log.info("Generating PDF receipt for Payment ID: {}", payment.getPaymentID());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // iText specific objects: PdfWriter writes the PDF, PdfDocument wraps it
        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // --- 1. Title and Header ---
            document.add(new Paragraph("Vector - Official Receipt")
                    .setFontSize(20)
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
            log.error("iText PDF Generation Error for Payment ID {}: {}", payment.getPaymentID(), e.getMessage(), e);
            throw new IOException("Failed to generate PDF receipt.", e);
        }

        log.debug("PDF generation complete for Payment ID: {}", payment.getPaymentID());
        return outputStream.toByteArray();
    }

    /**
     * Helper method to map Payment entity to PaymentDto
     */
    private PaymentDto mapToDto(Payment entity) {
        log.debug("Mapping Payment entity to PaymentDto for ID: {}", entity.getPaymentID());
        PaymentDto dto = new PaymentDto();
        dto.setPaymentID(entity.getPaymentID());
        dto.setRideID(entity.getRideID());
        dto.setUserID(entity.getUserID());
        dto.setAmount(entity.getAmount());
        dto.setMethod(entity.getMethod());
        dto.setStatus(entity.getStatus());
        dto.setTimestamp(entity.getTimestamp());
        dto.setPickupLocation(entity.getPickupLocation());
        dto.setDropLocation(entity.getDropLocation());
        dto.setReceipientEmail(entity.getReceipientEmail());
        return dto;
    }
}