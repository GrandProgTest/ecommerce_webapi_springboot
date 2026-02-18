package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources;

import java.time.Instant;

public record CategoryGraphQLResource(Long id, String name, Instant createdAt) {
}

