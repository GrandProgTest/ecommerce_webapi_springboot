package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.resources;

import java.time.Instant;
import java.util.List;

public record OrderGraphQLResource(String id, String userId, OrderUserGraphQLResource user, String cartId,
                                   String addressId, String discountCode, String status, String deliveryStatus,
                                   Double totalAmount, String clientSecret, List<OrderItemGraphQLResource> items,
                                   Instant createdAt, Instant paidAt) {
}

