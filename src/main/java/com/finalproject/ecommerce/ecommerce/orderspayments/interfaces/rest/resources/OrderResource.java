package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResource(Long id, Long userId, Long cartId, Long addressId, String discountCode, String status,
                            String deliveryStatus, BigDecimal totalAmount, String checkoutUrl,
                            List<OrderItemResource> items, Instant createdAt, Instant paidAt) {
}
