package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

public class DuplicateCategoryAssignmentException extends RuntimeException {
    public DuplicateCategoryAssignmentException(Long productId, Long categoryId) {
        super("Category with id " + categoryId + " is already assigned to product with id " + productId);
    }
}
