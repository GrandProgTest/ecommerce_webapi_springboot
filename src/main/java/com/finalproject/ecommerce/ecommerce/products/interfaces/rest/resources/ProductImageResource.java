package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;

import java.time.LocalDateTime;

public record ProductImageResource(
    Long id,
    Long productId,
    String imageUrl,
    Boolean isPrimary,
    LocalDateTime createdAt
) {}
