package com.finalproject.ecommerce.ecommerce.notifications.domain.model.commands;

import com.finalproject.ecommerce.ecommerce.notifications.domain.model.valueobjects.EmailTemplate;

import java.util.Map;

public record SendEmailCommand(
        String toEmail,
        EmailTemplate template,
        Map<String, Object> templateData,
        String customSubject
) {
    public SendEmailCommand {
        if (toEmail == null || toEmail.isBlank()) {
            throw new IllegalArgumentException("Recipient email cannot be null or empty");
        }
        if (template == null) {
            throw new IllegalArgumentException("Email template cannot be null");
        }
        if (templateData == null) {
            templateData = Map.of();
        }
    }

    public SendEmailCommand(String toEmail, EmailTemplate template, Map<String, Object> templateData) {
        this(toEmail, template, templateData, null);
    }

    public String getSubject() {
        return customSubject != null ? customSubject : template.getDefaultSubject();
    }
}

