package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources;

import java.util.List;

public record CartGraphQLResource(Long id, Long userId, String status, List<CartItemGraphQLResource> items,
                                  Integer totalItems, String createdAt, String updatedAt, String checkedOutAt) {
}
