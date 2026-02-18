package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;

import java.time.Instant;

public record ProductImageResource(Long id, Long productId, String imageUrl, Boolean isPrimary,
                                   Instant createdAt) {
}
