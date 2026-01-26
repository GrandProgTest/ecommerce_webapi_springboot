package com.finalproject.ecommerce.ecommerce.products.rest.resources;

import java.time.LocalDateTime;

public record ProductLikeResource(
    Long userId,
    Long productId,
    LocalDateTime likedAt
) {}
