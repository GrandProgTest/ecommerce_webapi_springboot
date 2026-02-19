package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources;

public record AddItemToCartGraphQLResource(Long userId, Long productId, Integer quantity) {
}
