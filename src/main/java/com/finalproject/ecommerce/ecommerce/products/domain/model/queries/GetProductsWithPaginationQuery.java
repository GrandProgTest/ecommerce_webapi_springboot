package com.finalproject.ecommerce.ecommerce.products.domain.model.queries;

public record GetProductsWithPaginationQuery(
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
    public GetProductsWithPaginationQuery {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (sortBy == null || sortBy.isBlank()) sortBy = "id";
        if (sortDirection == null || sortDirection.isBlank()) sortDirection = "asc";
    }
}
