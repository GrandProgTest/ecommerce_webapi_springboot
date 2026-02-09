package com.finalproject.ecommerce.ecommerce.carts.domain.exceptions;

import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.InvalidOperationException;

public class InvalidCartOperationException extends InvalidOperationException {
    public InvalidCartOperationException(String message) {
        super(message);
    }
}
