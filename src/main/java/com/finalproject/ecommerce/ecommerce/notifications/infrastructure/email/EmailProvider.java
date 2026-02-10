package com.finalproject.ecommerce.ecommerce.notifications.infrastructure.email;

public interface EmailProvider {
    /**
     * Send an email
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody Email body in HTML format
     * @return true if email was sent successfully
     */
    boolean sendEmail(String to, String subject, String htmlBody);
}

