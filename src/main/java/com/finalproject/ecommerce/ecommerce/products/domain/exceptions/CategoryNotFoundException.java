package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.ResourceNotFoundException;

public class CategoryNotFoundException extends ResourceNotFoundException {
    public CategoryNotFoundException(Long categoryId) {
        super("Category with ID " + categoryId + " not found");
    }
}
