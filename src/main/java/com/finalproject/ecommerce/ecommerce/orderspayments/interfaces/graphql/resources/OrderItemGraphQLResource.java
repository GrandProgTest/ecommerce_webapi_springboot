package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.resources;

public record OrderItemGraphQLResource(String id, String orderId, String productId, Double priceAtPurchase,
                                       Integer quantity) {
}

