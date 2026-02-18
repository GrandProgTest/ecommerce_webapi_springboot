package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources;

import java.time.Instant;

public record CartItemGraphQLResource(Long id, Long productId, Integer quantity, Instant createdAt, Instant updatedAt) {
}
