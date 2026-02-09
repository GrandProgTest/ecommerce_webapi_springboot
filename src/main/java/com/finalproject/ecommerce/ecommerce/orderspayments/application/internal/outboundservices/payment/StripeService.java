package com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.outboundservices.payment;

import com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.outboundservices.payment.dto.StripeResponse;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;


public interface StripeService {

    StripeResponse createCheckoutSession(Order order);

    String getPaymentStatus(String sessionId);

    String handleWebhookEvent(String payload, String signature);

    void cancelPaymentSession(String sessionId);
}
