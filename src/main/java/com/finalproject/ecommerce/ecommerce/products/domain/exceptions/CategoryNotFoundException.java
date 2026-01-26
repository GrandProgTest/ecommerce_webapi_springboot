package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long categoryId) {
        super("Category with ID " + categoryId + " not found");
    }
}
