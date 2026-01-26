package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

import java.math.BigDecimal;

public record UpdateProductCommand(
    Long productId,
    String name,
    String description,
    BigDecimal price,
    Integer stock
) {
}
