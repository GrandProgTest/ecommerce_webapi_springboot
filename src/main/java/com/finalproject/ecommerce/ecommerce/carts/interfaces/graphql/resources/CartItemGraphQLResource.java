package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources;

public record CartItemGraphQLResource(Long id, Long productId, Integer quantity, String createdAt, String updatedAt) {
}
