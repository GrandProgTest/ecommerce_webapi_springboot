package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductCommand(
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    List<Long> categoryIds) {
}
