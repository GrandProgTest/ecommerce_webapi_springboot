package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {
    public ProductNotFoundException(Long productId) {
        super("Product with ID " + productId + " not found");
    }
}
