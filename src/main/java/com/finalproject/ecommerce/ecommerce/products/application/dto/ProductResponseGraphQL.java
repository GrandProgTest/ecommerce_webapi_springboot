package com.finalproject.ecommerce.ecommerce.products.application.dto;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductResponseGraphQL(Long id, String name, String description, BigDecimal price, BigDecimal salePrice,
                                     Instant salePriceExpireDate, BigDecimal effectivePrice, Boolean hasActiveSalePrice,
                                     Integer stock, Boolean isActive, Boolean isDeleted, List<Long> categoryIds,
                                     Long createdByUserId, String primaryImageUrl, Integer likeCount,
                                     Instant createdAt, Instant updatedAt) {
    public static ProductResponseGraphQL fromEntity(Product product) {
        return new ProductResponseGraphQL(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSalePrice(),
                product.getSalePriceExpireDate(),
                product.getEffectivePrice(),
                product.hasActiveSalePrice(),
                product.getStock(),
                product.getIsActive(),
                product.getIsDeleted(),
                product.getCategoryIds(),
                product.getCreatedByUserId(),
                product.getPrimaryImageUrl(),
                product.getLikesCount(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}

