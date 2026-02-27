package com.finalproject.ecommerce.ecommerce.products.domain.model.queries;

public record GetProductsWithPaginationQuery(Long categoryId, Boolean isActive, int page, int size, String sortBy,
                                             String sortDirection) {
}

