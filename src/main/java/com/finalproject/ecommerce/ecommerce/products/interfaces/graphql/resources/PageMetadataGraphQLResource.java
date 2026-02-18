package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources;

public record PageMetadataGraphQLResource(
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}

