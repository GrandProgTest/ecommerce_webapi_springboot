package com.finalproject.ecommerce.ecommerce.carts.domain.model.commands;

public record UpdateCartItemQuantityCommand(
    Long userId,
    Long productId,
    Integer quantity
) {
    public UpdateCartItemQuantityCommand {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }
}
