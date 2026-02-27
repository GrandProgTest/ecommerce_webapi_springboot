package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries;

public record GetDiscountByCodeQuery(String code) {
    public GetDiscountByCodeQuery {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Discount code cannot be empty");
        }
    }
}

