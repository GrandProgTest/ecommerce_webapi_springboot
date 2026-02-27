package com.finalproject.ecommerce.ecommerce.iam.domain.exceptions;

public class InvalidActivationTokenException extends RuntimeException {
    public InvalidActivationTokenException() {
        super("Invalid or expired activation token");
    }

    public InvalidActivationTokenException(String message) {
        super(message);
    }
}

