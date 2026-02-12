package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands;

public record CancelOrderCommand(Long orderId) {
    public CancelOrderCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
    }
}
