package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.resources;

import java.time.Instant;

public record PaymentGraphQLResource(String id, String orderId, String stripePaymentIntentId, String status,
                                     Double amount, Instant createdAt) {
}

