package com.finalproject.ecommerce.ecommerce.orderspayments.domain.exceptions;

public class InvalidOrderOperationException extends RuntimeException {
    public InvalidOrderOperationException(String message) {
        super(message);
    }
}
