package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductInput(
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    List<Long> categoryIds
) {}
