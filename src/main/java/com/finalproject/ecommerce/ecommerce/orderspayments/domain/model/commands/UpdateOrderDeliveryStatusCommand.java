package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.DeliveryStatuses;

public record UpdateOrderDeliveryStatusCommand(Long orderId, DeliveryStatuses newDeliveryStatus) {
    public UpdateOrderDeliveryStatusCommand {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        if (newDeliveryStatus == null) {
            throw new IllegalArgumentException("New delivery status cannot be null");
        }
    }
}

