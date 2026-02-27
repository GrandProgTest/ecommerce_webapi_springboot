package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands;

public record CreateOrderFromCartCommand(
        Long userId,
        Long cartId,
        Long addressId,
        String discountCode
) {
    public CreateOrderFromCartCommand {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (cartId == null) {
            throw new IllegalArgumentException("Cart ID cannot be null");
        }
        if (addressId == null) {
            throw new IllegalArgumentException("Address ID cannot be null");
        }
    }
}
