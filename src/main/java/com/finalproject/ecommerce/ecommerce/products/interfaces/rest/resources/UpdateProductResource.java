package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;

import java.math.BigDecimal;

public record UpdateProductResource(
    String name,
    String description,
    BigDecimal price,
    Integer stock
) {}
