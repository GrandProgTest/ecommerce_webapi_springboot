package com.finalproject.ecommerce.ecommerce.notifications.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.notifications.domain.exceptions.EmailSendingException;
import com.finalproject.ecommerce.ecommerce.notifications.domain.model.commands.SendEmailCommand;
import com.finalproject.ecommerce.ecommerce.notifications.domain.model.valueobjects.EmailTemplate;
import com.finalproject.ecommerce.ecommerce.notifications.domain.services.EmailCommandService;
import com.finalproject.ecommerce.ecommerce.notifications.infrastructure.email.EmailProvider;
import com.finalproject.ecommerce.ecommerce.notifications.infrastructure.templates.EmailTemplateProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class EmailCommandServiceImpl implements EmailCommandService {

    private final EmailProvider emailProvider;
    private final EmailTemplateProcessor templateEngine;

    public EmailCommandServiceImpl(EmailProvider emailProvider, EmailTemplateProcessor templateEngine) {
        this.emailProvider = emailProvider;
        this.templateEngine = templateEngine;
    }

    @Override
    public boolean handle(SendEmailCommand command) {
        try {
            log.info("Preparing to send email to {} using template {}", command.toEmail(), command.template().getTemplateName());

            String htmlBody = templateEngine.processTemplate(command.template().getTemplateName(), command.templateData());

            boolean sent = emailProvider.sendEmail(command.toEmail(), command.getSubject(), htmlBody);

            if (sent) {
                log.info("Email sent successfully to {} with template {}", command.toEmail(), command.template().getTemplateName());
            } else {
                log.warn("Email sending failed for {} with template {}", command.toEmail(), command.template().getTemplateName());
            }

            return sent;

        } catch (Exception e) {
            log.error("Error sending email to {} with template {}: {}", command.toEmail(), command.template().getTemplateName(), e.getMessage(), e);
            throw new EmailSendingException("Failed to send email to " + command.toEmail(), e);
        }
    }

    @Async
    public CompletableFuture<Void> sendBatchEmailAsync(Set<String> recipientEmails, EmailTemplate template, String subject, Map<String, Object> templateData) {

        log.info("Starting async batch email sending to {} users with template {} (using BCC)", recipientEmails.size(), template.getTemplateName());

        try {
            String htmlBody = templateEngine.processTemplate(template.getTemplateName(), templateData);

            boolean sent = emailProvider.sendBatchEmail(recipientEmails, subject, htmlBody);

            if (sent) {
                log.info("Batch email sent successfully to {} recipients using BCC", recipientEmails.size());
            } else {
                log.warn("Failed to send batch email to {} recipients", recipientEmails.size());
            }

        } catch (Exception e) {
            log.error("Error sending batch email to {} recipients: {}", recipientEmails.size(), e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }
}
