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
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken, int expirationMinutes) {
        try {
            Map<String, Object> templateData = Map.of(
                    "username", userName,
                    "resetToken", resetToken,
                    "expirationMinutes", String.valueOf(expirationMinutes)
            );

            var command = new SendEmailCommand(toEmail, EmailTemplate.PASSWORD_RESET, templateData);
            emailCommandService.handle(command);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    public void sendPasswordChangedEmail(String toEmail, String userName, String changeDateTime) {
        try {
            Map<String, Object> templateData = Map.of(
                    "userName", userName,
                    "changeDateTime", changeDateTime
            );

            var command = new SendEmailCommand(toEmail, EmailTemplate.PASSWORD_CHANGED, templateData);
            emailCommandService.handle(command);
        } catch (Exception e) {
            log.error("Failed to send password changed email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String userName, String storeUrl) {
        try {
            Map<String, Object> templateData = Map.of(
                    "userName", userName,
                    "storeUrl", storeUrl
            );

            var command = new SendEmailCommand(toEmail, EmailTemplate.WELCOME, templateData);
            emailCommandService.handle(command);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    public void sendOrderConfirmationEmail(String toEmail, Map<String, Object> orderData) {
        try {
            var command = new SendEmailCommand(toEmail, EmailTemplate.ORDER_CONFIRMATION, orderData);
            emailCommandService.handle(command);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}: {}", toEmail, e.getMessage());
        }
    }


    @Override
    public void sendLowStockAlert(String toEmail, String productName, int currentStock) {
        try {
            Map<String, Object> templateData = Map.of(
                    "productName", productName,
                    "currentStock", String.valueOf(currentStock)
            );

            var command = new SendEmailCommand(toEmail, EmailTemplate.LOW_STOCK_ALERT, templateData);
            emailCommandService.handle(command);
        } catch (Exception e) {
            log.error("Failed to send low stock alert email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    public void sendLowStockAlertBatch(Set<String> recipientEmails,
                                       String productName,
                                       int currentStock) {
        try {
            Map<String, Object> templateData = Map.of(
                    "productName", productName,
                    "currentStock", String.valueOf(currentStock)
            );

            emailCommandServiceImpl.sendBatchEmailAsync(
                    recipientEmails,
                    EmailTemplate.LOW_STOCK_ALERT,
                    EmailTemplate.LOW_STOCK_ALERT.getDefaultSubject(),
                    templateData
            );

        } catch (Exception e) {
            log.error("Failed to queue batch low stock alerts: {}", e.getMessage());
            CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public void sendOrderStatusUpdate(String toEmail, String username, Long orderId,
                                          String orderStatus, String statusMessage,
                                          String totalAmount, String orderDate) {
        try {
            Map<String, Object> templateData = Map.of(
                    "username", username,
                    "orderId", orderId.toString(),
                    "orderStatus", orderStatus,
                    "statusMessage", statusMessage,
                    "totalAmount", totalAmount,
                    "orderDate", orderDate,
                    "orderUrl", "http://localhost:8080/api/v1/orders/user/" + orderId
            );

            var command = new SendEmailCommand(toEmail, EmailTemplate.ORDER_STATUS_UPDATE, templateData);
            emailCommandService.handle(command);
        } catch (Exception e) {
            log.error("Failed to send order status update email to {}: {}", toEmail, e.getMessage());
        }
    }
}


