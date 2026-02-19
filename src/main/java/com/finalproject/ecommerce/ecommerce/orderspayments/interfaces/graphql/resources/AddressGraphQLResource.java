package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.resources;

import java.time.Instant;

public record AddressGraphQLResource(String id, String userId, String street, String city, String state, String country,
                                     String postalCode, Boolean isDefault, Instant createdAt) {
}

