package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

public record AssignCategoryToProductCommand(
    Long productId,
    Long categoryId
) {
}
