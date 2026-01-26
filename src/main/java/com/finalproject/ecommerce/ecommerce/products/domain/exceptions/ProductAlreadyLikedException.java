package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

public class ProductAlreadyLikedException extends RuntimeException {
    public ProductAlreadyLikedException(Long userId, Long productId) {
        super(String.format("User %d already liked product %d", userId, productId));
    }
}
