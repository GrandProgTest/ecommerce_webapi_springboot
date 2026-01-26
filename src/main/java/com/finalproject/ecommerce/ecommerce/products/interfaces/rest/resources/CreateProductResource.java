package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductResource(
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    List<Long> categoryIds,
    Long createdByUserId
) {}
