package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources;

import java.math.BigDecimal;

public record UpdateProductInput(
    String name,
    String description,
    BigDecimal price,
    Integer stock
) {}
