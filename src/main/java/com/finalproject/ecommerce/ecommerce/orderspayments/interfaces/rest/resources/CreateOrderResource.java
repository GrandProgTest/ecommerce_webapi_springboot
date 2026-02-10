package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources;

public record CreateOrderResource(Long userId, Long cartId, Long addressId, String discountCode) {
}
