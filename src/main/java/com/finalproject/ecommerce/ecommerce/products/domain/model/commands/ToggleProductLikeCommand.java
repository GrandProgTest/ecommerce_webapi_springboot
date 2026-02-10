package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

public record ToggleProductLikeCommand(Long productId) {
    public ToggleProductLikeCommand {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
    }
}
