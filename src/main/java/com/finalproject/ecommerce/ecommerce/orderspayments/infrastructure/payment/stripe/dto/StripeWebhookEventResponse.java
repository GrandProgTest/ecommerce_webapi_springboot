package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StripeWebhookEventResponse {
    private final String eventType;
    private final String eventId;
    private final String sessionId;
    private final String customerEmail;
    private final String paymentStatus;
    private final Long amountTotal;
    private final String currency;
    private final String orderId;
}

