package com.finalproject.ecommerce.ecommerce.notifications.application.acl.services;

import com.finalproject.ecommerce.ecommerce.notifications.application.internal.commandservices.EmailCommandServiceImpl;
import com.finalproject.ecommerce.ecommerce.notifications.domain.model.commands.SendEmailCommand;
import com.finalproject.ecommerce.ecommerce.notifications.domain.model.valueobjects.EmailTemplate;
import com.finalproject.ecommerce.ecommerce.notifications.domain.services.EmailCommandService;
import com.finalproject.ecommerce.ecommerce.notifications.interfaces.acl.NotificationContextFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class NotificationContextFacadeImpl implements NotificationContextFacade {

    private final EmailCommandService emailCommandService;
    private final EmailCommandServiceImpl emailCommandServiceImpl;

    public NotificationContextFacadeImpl(EmailCommandService emailCommandService,
                                         EmailCommandServiceImpl emailCommandServiceImpl) {
        this.emailCommandService = emailCommandService;
        this.emailCommandServiceImpl = emailCommandServiceImpl;
    }

    @Override
    public boolean sendPasswordResetEmail(String toEmail, String userName, String resetLink, int expirationMinutes) {
        try {
            Map<String, Object> templateData = Map.of(
                    "userName", userName,
                    "resetLink", resetLink,
                    "expirationMinutes", String.valueOf(expirationMinutes)
            );

            var command = new SendEmailCommand(toEmail, EmailTemplate.PASSWORD_RESET, templateData);
            return emailCommandService.handle(command);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendPasswordChangedEmail(String toEmail, String userName, String changeDateTime) {
        try {
            Map<String, Object> templateData = Map.of(
                    "userName", userName,
                    "changeDateTime", changeDateTime
            );

            var command = new SendEmailCommand(toEmail, EmailTemplate.PASSWORD_CHANGED, templateData);
            return emailCommandService.handle(command);
        } catch (Exception e) {
            log.error("Failed to send password changed email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendWelcomeEmail(String toEmail, String userName, String storeUrl) {
        try {
            Map<String, Object> templateData = Map.of(
                    "userName", userName,
                    "storeUrl", storeUrl
            );

            var command = new SendEmailCommand(toEmail, EmailTemplate.WELCOME, templateData);
            return emailCommandService.handle(command);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendOrderConfirmationEmail(String toEmail, Map<String, Object> orderData) {
        try {
            var command = new SendEmailCommand(toEmail, EmailTemplate.ORDER_CONFIRMATION, orderData);
            return emailCommandService.handle(command);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendEmail(String toEmail, EmailTemplate template, Map<String, Object> templateData) {
        try {
            var command = new SendEmailCommand(toEmail, template, templateData);
            return emailCommandService.handle(command);
        } catch (Exception e) {
            log.error("Failed to send email to {} with template {}: {}", toEmail, template, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendLowStockAlert(String toEmail, String productName, int currentStock) {
        try {
            Map<String, Object> templateData = Map.of(
                    "productName", productName,
                    "currentStock", String.valueOf(currentStock)
            );

            var command = new SendEmailCommand(toEmail, EmailTemplate.LOW_STOCK_ALERT, templateData);
            return emailCommandService.handle(command);
        } catch (Exception e) {
            log.error("Failed to send low stock alert email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    @Override
    public CompletableFuture<Void> sendLowStockAlertBatch(Set<String> recipientEmails,
                                                          String productName,
                                                          int currentStock) {
        try {
            Map<String, Object> templateData = Map.of(
                    "productName", productName,
                    "currentStock", String.valueOf(currentStock)
            );

            return emailCommandServiceImpl.sendBatchEmailAsync(
                    recipientEmails,
                    EmailTemplate.LOW_STOCK_ALERT,
                    EmailTemplate.LOW_STOCK_ALERT.getDefaultSubject(),
                    templateData
            );

        } catch (Exception e) {
            log.error("Failed to queue batch low stock alerts: {}", e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }
}


