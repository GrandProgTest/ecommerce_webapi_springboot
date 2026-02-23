package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects;


public enum PaymentIntentStatuses {
    REQUIRES_PAYMENT_METHOD,
    REQUIRES_CONFIRMATION,
    REQUIRES_ACTION,
    PROCESSING,
    REQUIRES_CAPTURE,
    CANCELED,
    SUCCEEDED
}

