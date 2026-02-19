package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductResource(Long id, String name, String description, BigDecimal price,
                              BigDecimal salePrice, Instant salePriceExpireDate,
                              BigDecimal effectivePrice, Boolean hasActiveSalePrice,
                              Integer stock, Boolean isActive, Boolean isDeleted,
                              List<Long> categoryIds, Long createdByUserId, String primaryImageUrl) {
}
