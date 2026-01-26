package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("Product with ID " + productId + " not found");
    }
}
