package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources;

public record UpdateCartItemQuantityGraphQLResource(Long userId, Long cartItemId, Integer quantity) {
}

