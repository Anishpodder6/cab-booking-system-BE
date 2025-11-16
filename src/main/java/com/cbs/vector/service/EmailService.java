package com.cbs.vector.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends an email with a PDF attachment.
     * @param to The recipient's email address.
     * @param subject The email subject.
     * @param body The email body/content.
     * @param pdfBytes The byte array of the PDF file.
     * @param filename The desired filename for the attachment.
     * @throws MessagingException if the mail cannot be sent.
     */
    public void sendEmailWithAttachment(String to, String subject, String body, byte[] pdfBytes, String filename) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        // true = multipart message to support attachments
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // ⚠️ You should configure a 'from' address in application.properties or set it here
        helper.setFrom("anishpodder6@gmail.com");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true); // true = set the content as HTML

        // Attach the PDF
        helper.addAttachment(filename, new ByteArrayResource(pdfBytes));

        mailSender.send(message);
    }
}