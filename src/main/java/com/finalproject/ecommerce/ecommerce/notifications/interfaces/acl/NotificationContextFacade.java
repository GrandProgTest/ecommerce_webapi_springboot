package com.finalproject.ecommerce.ecommerce.notifications.interfaces.acl;

import java.util.Map;
import java.util.Set;


public interface NotificationContextFacade {
    
    void sendPasswordResetEmail(String toEmail, String userName, String resetToken, int expirationMinutes);

    void sendPasswordChangedEmail(String toEmail, String userName, String changeDateTime);

    void sendWelcomeEmail(String toEmail, String userName, String activationUrl);

    void sendOrderConfirmationEmail(String toEmail, Map<String, Object> orderData);

    void sendLowStockAlert(String toEmail, String productName, int currentStock);

    void sendLowStockAlertBatch(Set<String> recipientEmails, String productName, int currentStock);

    void sendOrderStatusUpdate(String toEmail, String username, Long orderId, String orderStatus,
                                   String statusMessage, String totalAmount, String orderDate);
}

