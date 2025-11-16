package com.cbs.vector.PaymentTest;


import com.cbs.vector.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the EmailService class using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    // Mock the dependency (JavaMailSender)
    @Mock
    private JavaMailSender mailSender;

    // Mock the internal object created by the mailSender
    @Mock
    private MimeMessage mimeMessage;

    // Inject the mocks into the service class being tested
    @InjectMocks
    private EmailService emailService;

    private static final String TO = "recipient@example.com";
    private static final String SUBJECT = "Monthly Report";
    private static final String BODY = "<h1>Please find the attached PDF report.</h1>";
    private static final byte[] PDF_BYTES = "DUMMY_PDF_CONTENT".getBytes();
    private static final String FILENAME = "report_2024.pdf";

    /**
     * Tests that the email sending logic executes successfully, verifying that
     * the createMimeMessage and send methods on JavaMailSender are called exactly once.
     * This confirms the service attempts to send the email.
     */
    @Test
    void sendEmailWithAttachment_shouldSendEmailSuccessfully() throws MessagingException {
        // Arrange
        // When mailSender.createMimeMessage() is called, return our mock MimeMessage
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendEmailWithAttachment(TO, SUBJECT, BODY, PDF_BYTES, FILENAME);

        // Assert
        // 1. Verify that the MimeMessage was created.
        verify(mailSender, times(1)).createMimeMessage();

        // 2. Verify that the mailSender.send method was called with the created message.
        // (This is the primary verification point for a Spring mail service unit test)
        verify(mailSender, times(1)).send(mimeMessage);
    }

    /**
     * Tests the scenario where a failure occurs during the creation of the MimeMessage.
     * The service method, as written, allows the unchecked Spring exception (MailParseException)
     * to bubble up. We update the test to expect this specific runtime exception.
     */
    @Test
    void sendEmailWithAttachment_shouldThrowMailParseException_whenMessageCreationFails() {
        // Arrange
        // Simulate a failure when creating the MimeMessage, which throws an unchecked exception (MailParseException).
        when(mailSender.createMimeMessage()).thenThrow(new MailParseException("Simulated message creation error"));

        // Act & Assert
        // Verify that calling the service method throws the actual RuntimeException (MailParseException).
        assertThrows(MailParseException.class, () -> {
            emailService.sendEmailWithAttachment(TO, SUBJECT, BODY, PDF_BYTES, FILENAME);
        });

        // Verify that the send operation was never attempted
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}