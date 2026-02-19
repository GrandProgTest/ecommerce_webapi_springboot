package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;

public record SetProductSalePriceResource(BigDecimal salePrice, Instant salePriceExpireDate) {
}
