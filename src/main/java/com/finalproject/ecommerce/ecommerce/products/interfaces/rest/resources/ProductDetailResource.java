package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailResource(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        Boolean isActive,
        List<Long> categoryIds,
        Long createdByUserId,
        List<ProductImageResource> images
) {
}

