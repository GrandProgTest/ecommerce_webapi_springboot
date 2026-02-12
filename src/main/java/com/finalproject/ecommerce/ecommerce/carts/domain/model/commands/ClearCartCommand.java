package com.finalproject.ecommerce.ecommerce.carts.domain.model.commands;

public record ClearCartCommand(Long userId) {
    public ClearCartCommand {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }
}
