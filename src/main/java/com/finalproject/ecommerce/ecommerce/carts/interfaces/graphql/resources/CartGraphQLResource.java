package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources;

import java.time.Instant;
import java.util.List;

public record CartGraphQLResource(Long id, Long userId, String status, List<CartItemGraphQLResource> items,
                                  Integer totalItems, Instant createdAt, Instant updatedAt, Instant checkedOutAt) {
}
