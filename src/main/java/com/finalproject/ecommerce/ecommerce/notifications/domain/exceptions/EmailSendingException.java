package com.finalproject.ecommerce.ecommerce.notifications.domain.exceptions;

public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}

