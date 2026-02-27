package com.finalproject.ecommerce.ecommerce.orderspayments.domain.exceptions;

import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.ResourceNotFoundException;

public class OrderNotFoundException extends ResourceNotFoundException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
