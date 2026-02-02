package com.finalproject.ecommerce.ecommerce.carts.domain.exceptions;

public class InvalidCartOperationException extends RuntimeException {
    public InvalidCartOperationException(String message) {
        super(message);
    }
}
