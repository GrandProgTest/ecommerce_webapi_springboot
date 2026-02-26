package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands;

public record ConfirmPaymentCommand(Long orderId, String paymentMethodId) {
}

