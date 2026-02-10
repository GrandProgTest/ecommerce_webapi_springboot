package com.finalproject.ecommerce.ecommerce.notifications.domain.exceptions;

public class TemplateNotFoundException extends RuntimeException {
    public TemplateNotFoundException(String templateName) {
        super("Email template not found: " + templateName);
    }
}

