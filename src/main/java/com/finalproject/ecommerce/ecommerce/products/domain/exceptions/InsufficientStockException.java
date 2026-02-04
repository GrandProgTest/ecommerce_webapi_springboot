package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId, int requested, int available) {
        super(String.format("Insufficient stock for product %d. Requested: %d, Available: %d", productId, requested, available));
    }
}
