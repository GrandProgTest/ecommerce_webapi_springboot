package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.dto;

public record PaymentIntentResponse(String paymentIntentId, String clientSecret, String status, String message) {
}

