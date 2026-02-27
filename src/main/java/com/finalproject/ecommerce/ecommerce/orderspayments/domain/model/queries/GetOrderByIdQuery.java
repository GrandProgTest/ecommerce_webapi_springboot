package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries;

public record GetOrderByIdQuery(Long orderId) {
    public GetOrderByIdQuery {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
    }
}
