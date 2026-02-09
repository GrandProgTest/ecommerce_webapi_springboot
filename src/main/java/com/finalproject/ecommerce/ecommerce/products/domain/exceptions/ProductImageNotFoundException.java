package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.ResourceNotFoundException;

public class ProductImageNotFoundException extends ResourceNotFoundException {
    public ProductImageNotFoundException(Long imageId) {
        super("Product image with ID " + imageId + " not found");
    }
}
