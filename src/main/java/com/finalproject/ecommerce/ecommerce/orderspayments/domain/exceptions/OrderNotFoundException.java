package com.finalproject.ecommerce.ecommerce.orderspayments.domain.exceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
