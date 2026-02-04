package com.socitymanager.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.socitymanager.model.ExpenseRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ReceiptService {
    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);
    private final JavaMailSender mailSender;

    public ReceiptService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String createReceiptAndEmail(ExpenseRequest request) {
        String receiptNumber = "RCPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        byte[] pdfBytes = generatePdfReceipt(request, receiptNumber);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(request.getEmail());
            helper.setSubject("Maintenance Receipt " + receiptNumber);
            helper.setText(buildEmailBody(request, receiptNumber), false);
            helper.addAttachment("receipt-" + receiptNumber + ".pdf", () -> pdfBytes);
            mailSender.send(message);
        } catch (MessagingException ex) {
            logger.warn("Email sending failed, check mail configuration", ex);
        }

        return receiptNumber;
    }

    private byte[] generatePdfReceipt(ExpenseRequest request, String receiptNumber) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            document.add(new Paragraph("Maintenance Payment Receipt", titleFont));
            document.add(new Paragraph("Receipt: " + receiptNumber));
            document.add(new Paragraph("Date: " + LocalDate.now()));
            document.add(new Paragraph("Apartment: " + request.getApartmentNumber()));
            document.add(new Paragraph("Resident: " + request.getResidentName()));
            document.add(new Paragraph("Email: " + request.getEmail()));
            document.add(new Paragraph("Description: " + request.getDescription()));
            document.add(new Paragraph("Amount Paid: " + request.getAmount()));
            document.close();

            return outputStream.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Failed to generate receipt PDF", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Unexpected error while generating PDF", ex);
        }
    }

    private String buildEmailBody(ExpenseRequest request, String receiptNumber) {
        return String.format(
                "Hello %s,%n%nThank you for your maintenance payment.%nReceipt: %s%nApartment: %s%nAmount: %.2f%n%nRegards,%nSociety Manager",
                request.getResidentName(),
                receiptNumber,
                request.getApartmentNumber(),
                request.getAmount()
        );
    }
}
