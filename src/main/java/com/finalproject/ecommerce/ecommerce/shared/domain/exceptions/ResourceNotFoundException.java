package com.finalproject.ecommerce.ecommerce.shared.domain.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, Long resourceId) {
        super(String.format("%s with id %d not found", resourceType, resourceId));
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s with id %s not found", resourceType, resourceId));
    }
}

