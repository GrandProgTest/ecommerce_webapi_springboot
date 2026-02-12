package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

public class ProductInOrdersException extends RuntimeException {
    public ProductInOrdersException(Long productId) {
        super("Product with id %d cannot be deleted because it is part of pending orders".formatted(productId));
    }
}

