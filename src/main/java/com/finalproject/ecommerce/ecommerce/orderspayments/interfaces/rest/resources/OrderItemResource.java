package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources;

import java.math.BigDecimal;

public record OrderItemResource(
        Long id,
        Long productId,
        BigDecimal priceAtPurchase,
        Integer quantity,
        BigDecimal subtotal
) {
}
