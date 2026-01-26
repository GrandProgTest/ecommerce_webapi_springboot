package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

public class ProductNotLikedException extends RuntimeException {
    public ProductNotLikedException(Long userId, Long productId) {
        super(String.format("User %d has not liked product %d", userId, productId));
    }
}
