package com.finalproject.ecommerce.ecommerce.notifications.infrastructure.email;

import java.util.Set;

public interface EmailProvider {

    boolean sendEmail(String to, String subject, String htmlBody);

    boolean sendBatchEmail(Set<String> recipients, String subject, String htmlBody);
}

