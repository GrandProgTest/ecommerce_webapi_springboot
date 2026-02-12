package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.BusinessRuleException;

public class DuplicateCategoryAssignmentException extends BusinessRuleException {
    public DuplicateCategoryAssignmentException(Long productId, Long categoryId) {
        super("Category with id " + categoryId + " is already assigned to product with id " + productId);
    }
}
