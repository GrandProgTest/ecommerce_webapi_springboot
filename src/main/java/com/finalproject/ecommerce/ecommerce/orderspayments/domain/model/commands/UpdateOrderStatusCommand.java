package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.OrderStatuses;

public record UpdateOrderStatusCommand(Long orderId, OrderStatuses newStatus) {
    public UpdateOrderStatusCommand {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        if (newStatus != OrderStatuses.SHIPPED && newStatus != OrderStatuses.DELIVERED) {
            throw new IllegalArgumentException("Only SHIPPED and DELIVERED statuses are allowed for updates");
        }
    }
}

