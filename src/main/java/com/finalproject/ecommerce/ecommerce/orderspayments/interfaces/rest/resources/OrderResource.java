package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public record OrderResource(
        Long id,
        Long userId,
        Long cartId,
        Long addressId,
        String discountCode,
        String status,
        BigDecimal totalAmount,
        List<OrderItemResource> items,
        Date createdAt
) {
}
