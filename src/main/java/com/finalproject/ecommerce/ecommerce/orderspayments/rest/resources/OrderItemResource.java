package com.finalproject.ecommerce.ecommerce.orderspayments.rest.resources;

import java.math.BigDecimal;

public record OrderItemResource(
        Long id,
        Long productId,
        BigDecimal priceAtPurchase,
        Integer quantity,
        BigDecimal subtotal
) {
}
