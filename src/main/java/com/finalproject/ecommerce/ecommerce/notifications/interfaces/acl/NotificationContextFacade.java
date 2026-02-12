package com.finalproject.ecommerce.ecommerce.notifications.interfaces.acl;

import com.finalproject.ecommerce.ecommerce.notifications.domain.model.valueobjects.EmailTemplate;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


public interface NotificationContextFacade {
    
    boolean sendPasswordResetEmail(String toEmail, String userName, String resetLink, int expirationMinutes);
    
    boolean sendPasswordChangedEmail(String toEmail, String userName, String changeDateTime);
    
    boolean sendWelcomeEmail(String toEmail, String userName, String storeUrl);

    boolean sendOrderConfirmationEmail(String toEmail, Map<String, Object> orderData);
    
    boolean sendEmail(String toEmail, EmailTemplate template, Map<String, Object> templateData);

    boolean sendLowStockAlert(String toEmail, String productName, int currentStock);

    CompletableFuture<Void> sendLowStockAlertBatch(Set<String> recipientEmails, String productName, int currentStock);
}

