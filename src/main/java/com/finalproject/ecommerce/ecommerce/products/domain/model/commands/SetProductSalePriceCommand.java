package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

import java.math.BigDecimal;
import java.time.Instant;

public record SetProductSalePriceCommand(Long productId, BigDecimal salePrice, Instant salePriceExpireDate) {
}

