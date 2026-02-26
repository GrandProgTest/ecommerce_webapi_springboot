package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

public record SoftDeleteProductCommand(Long productId) {
    public SoftDeleteProductCommand {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be provided and must be positive");
        }
    }
}

