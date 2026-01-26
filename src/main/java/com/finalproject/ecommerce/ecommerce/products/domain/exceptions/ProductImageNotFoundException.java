package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

public class ProductImageNotFoundException extends RuntimeException {
    public ProductImageNotFoundException(Long imageId) {
        super("Product image with ID " + imageId + " not found");
    }
}
