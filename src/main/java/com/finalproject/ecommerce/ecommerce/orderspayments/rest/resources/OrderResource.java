package com.finalproject.ecommerce.ecommerce.orderspayments.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResource(
        Long id,
        Long userId,
        Long cartId,
        Long addressId,
        String discountCode,
        String status,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        List<OrderItemResource> items,
        LocalDateTime createdAt
) {
}
