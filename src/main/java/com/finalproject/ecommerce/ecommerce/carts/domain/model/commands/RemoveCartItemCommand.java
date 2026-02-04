package com.finalproject.ecommerce.ecommerce.carts.domain.model.commands;

public record RemoveCartItemCommand(
    Long userId,
    Long cartItemId
) {
    public RemoveCartItemCommand {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (cartItemId == null) {
            throw new IllegalArgumentException("Cart item ID cannot be null");
        }
    }
}
