package com.finalproject.ecommerce.ecommerce.notifications.infrastructure.email;

import com.finalproject.ecommerce.ecommerce.notifications.domain.exceptions.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class GmailEmailProvider implements EmailProvider {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public GmailEmailProvider(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public boolean sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);

            log.info("Email sent successfully via Gmail SMTP to: {}", to);
            return true;

        } catch (MessagingException e) {
            log.error("Failed to send email via Gmail SMTP to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send email via Gmail SMTP: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email via Gmail SMTP to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send email via Gmail SMTP: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean sendBatchEmail(Set<String> recipients, String subject, String htmlBody) {
        if (recipients == null || recipients.isEmpty()) {
            log.warn("No recipients provided for batch email");
            return false;
        }

        try {
            Set<String> uniqueRecipients = new HashSet<>(recipients);

            if (uniqueRecipients.size() < recipients.size()) {
                log.info("Removed {} duplicate email(s) from batch recipients",
                        recipients.size() - uniqueRecipients.size());
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(fromEmail);

            String[] bccArray = uniqueRecipients.toArray(new String[0]);
            helper.setBcc(bccArray);

            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);

            log.info("Batch email sent successfully via Gmail SMTP to {} unique recipients",
                    uniqueRecipients.size());
            return true;

        } catch (MessagingException e) {
            log.error("Failed to send batch email via Gmail SMTP to {} recipients: {}",
                    recipients.size(), e.getMessage(), e);
            throw new EmailSendingException("Failed to send batch email via Gmail SMTP: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending batch email via Gmail SMTP: {}", e.getMessage(), e);
            throw new EmailSendingException("Failed to send batch email via Gmail SMTP: " + e.getMessage(), e);
        }
    }
}

