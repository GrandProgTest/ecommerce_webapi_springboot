package com.finalproject.ecommerce.ecommerce.carts.domain.model.commands;

public record CheckoutCartCommand(Long userId) {
    public CheckoutCartCommand {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }
}
