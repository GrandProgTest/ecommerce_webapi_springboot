package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries;

public record GetDiscountByIdQuery(Long discountId) {
    public GetDiscountByIdQuery {
        if (discountId == null) {
            throw new IllegalArgumentException("Discount ID cannot be null");
        }
    }
}

