package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

public class InvalidImageSizeException extends RuntimeException {
    public InvalidImageSizeException(String contentType) {
        super("Invalid image size. Content type: " + contentType + " Image size must be less than 5MB.");
    }
}
