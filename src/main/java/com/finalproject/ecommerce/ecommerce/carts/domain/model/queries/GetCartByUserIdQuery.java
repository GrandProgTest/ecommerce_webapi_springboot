package com.finalproject.ecommerce.ecommerce.carts.domain.model.queries;

public record GetCartByUserIdQuery(Long userId) {
    public GetCartByUserIdQuery {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }
}
