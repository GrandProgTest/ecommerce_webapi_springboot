package com.finalproject.ecommerce.ecommerce.carts.domain.model.commands;

public record RemoveProductFromCartCommand(Long userId, Long productId) {
    public RemoveProductFromCartCommand {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
    }
}
