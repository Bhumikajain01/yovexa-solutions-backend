package com.yovexa.solutions.service.impl;

import com.yovexa.solutions.model.ContactInquiry;
import com.yovexa.solutions.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.mail.notification-recipient:${spring.mail.username:}}")
    private String notificationRecipient;

    @Async
    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        if (mailFrom == null || mailFrom.isBlank()) {
            log.warn("Email sender (spring.mail.username) is not configured. Skipping email to: {}", to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email successfully sent to: {}", to);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }

    @Async
    @Override
    public void sendInquiryNotification(ContactInquiry inquiry) {
        String recipient = (notificationRecipient != null && !notificationRecipient.isBlank())
                ? notificationRecipient
                : mailFrom;

        if (recipient == null || recipient.isBlank()) {
            log.warn("No recipient configured for inquiry notification. Skipping email alert.");
            return;
        }

        String safeName = inquiry.getFullName() != null
                ? com.yovexa.solutions.util.MongoSecurityUtils.sanitizeHeader(inquiry.getFullName())
                : "Website Visitor";
        String subject = "New Website Inquiry: " + safeName;
        String body = String.format(
                "You have received a new contact inquiry on Yovexa Solutions:\n\n" +
                "Name: %s\n" +
                "Email: %s\n" +
                "Phone: %s\n" +
                "Company: %s\n" +
                "Service: %s\n" +
                "Budget: %s\n\n" +
                "Message:\n%s\n\n" +
                "Received at: %s",
                inquiry.getFullName(),
                inquiry.getEmail(),
                inquiry.getPhone() != null ? inquiry.getPhone() : "N/A",
                inquiry.getCompanyName() != null ? inquiry.getCompanyName() : "N/A",
                inquiry.getService() != null ? inquiry.getService() : "N/A",
                inquiry.getBudget() != null ? inquiry.getBudget() : "N/A",
                inquiry.getMessage(),
                inquiry.getCreatedAt()
        );

        sendSimpleEmail(recipient, subject, body);
    }
}
