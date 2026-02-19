package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources;

import java.time.Instant;
import java.util.List;

public record ProductGraphQLResource(Long id, String name, String description, Double price,
                                     Double salePrice, Instant salePriceExpireDate,
                                     Double effectivePrice, Boolean hasActiveSalePrice,
                                     Integer stock, Boolean isActive, List<Long> categoryIds,
                                     Long createdByUserId, Integer likeCount,
                                     Boolean isLikedByCurrentUser, Boolean isInStock,
                                     String primaryImageUrl, Instant createdAt, Instant updatedAt) {
}

