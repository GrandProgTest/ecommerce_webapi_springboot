package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

public class DuplicateCategoryException extends RuntimeException {
    public DuplicateCategoryException(String categoryName) {
        super("Category with name '" + categoryName + "' already exists");
    }
}
