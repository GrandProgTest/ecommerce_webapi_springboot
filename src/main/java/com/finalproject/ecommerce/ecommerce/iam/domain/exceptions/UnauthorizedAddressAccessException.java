package com.finalproject.ecommerce.ecommerce.iam.domain.exceptions;

public class UnauthorizedAddressAccessException extends RuntimeException {
    public UnauthorizedAddressAccessException() {
        super("You are not authorized to access this address");
    }
}
