package com.finalproject.ecommerce.ecommerce.orderspayments.domain.exceptions;

import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.InvalidOperationException;

public class InvalidOrderOperationException extends InvalidOperationException {
    public InvalidOrderOperationException(String message) {
        super(message);
    }
}
