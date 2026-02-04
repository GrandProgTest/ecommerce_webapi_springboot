package com.finalproject.ecommerce.ecommerce.carts.domain.model.commands;

public record UpdateCartItemQuantityByCartItemIdCommand(Long userId, Long cartItemId, Integer quantity) {
    public UpdateCartItemQuantityByCartItemIdCommand {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (cartItemId == null) {
            throw new IllegalArgumentException("Cart item ID cannot be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }
}
