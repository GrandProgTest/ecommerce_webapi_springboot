package com.finalproject.ecommerce.ecommerce.shared.domain.exceptions;

public class InvalidPageSizeException extends InvalidOperationException {
    public InvalidPageSizeException(int requestedSize) {
        super("Invalid page size: " + requestedSize + ". Allowed values are: 20, 50, 100");
    }
}
