package com.finalproject.ecommerce.ecommerce.orderspayments.application.ports.out;

import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.dto.PaymentIntentResponse;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;


public interface PaymentProvider {

    PaymentIntentResponse initiatePayment(Order order);

    void cancelPayment(String paymentIntentId);
}

