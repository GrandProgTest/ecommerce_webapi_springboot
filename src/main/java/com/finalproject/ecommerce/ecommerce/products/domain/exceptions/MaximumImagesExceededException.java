package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

public class MaximumImagesExceededException extends RuntimeException {
    public MaximumImagesExceededException(Long productId, int currentCount, int maxAllowed) {
        super("Product " + productId + " has reached the maximum number of images." +
              "Current: " + currentCount + ", Maximum allowed: " + maxAllowed);
    }
}

