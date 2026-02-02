package com.finalproject.ecommerce.ecommerce.carts.domain.model.queries;

public record GetCartByIdQuery(Long cartId) {
    public GetCartByIdQuery {
        if (cartId == null) {
            throw new IllegalArgumentException("Cart ID cannot be null");
        }
    }
}
