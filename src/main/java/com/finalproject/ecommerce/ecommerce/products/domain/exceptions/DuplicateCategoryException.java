package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.BusinessRuleException;

public class DuplicateCategoryException extends BusinessRuleException {
    public DuplicateCategoryException(String categoryName) {
        super("Category with name '" + categoryName + "' already exists");
    }
}
