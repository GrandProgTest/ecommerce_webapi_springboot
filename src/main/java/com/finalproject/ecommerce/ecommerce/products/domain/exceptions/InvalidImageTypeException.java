package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

public class InvalidImageTypeException extends RuntimeException {
    public InvalidImageTypeException(String contentType) {
        super("Invalid image type: " + contentType + ". Only JPEG and PNG images are allowed.");
    }
}

