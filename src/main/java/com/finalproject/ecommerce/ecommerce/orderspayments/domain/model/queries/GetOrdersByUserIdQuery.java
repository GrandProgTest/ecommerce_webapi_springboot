package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries;

public record GetOrdersByUserIdQuery(Long userId) {
    public GetOrdersByUserIdQuery {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }
}
