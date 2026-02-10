package com.finalproject.ecommerce.ecommerce.orderspayments.application.dtos;

public record PaymentSessionDto(
        String sessionId,
        String checkoutUrl,
        String status,
        String message
) {
}

