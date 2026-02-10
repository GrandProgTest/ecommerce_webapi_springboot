package com.finalproject.ecommerce.ecommerce.orderspayments.application.ports.out;

import com.finalproject.ecommerce.ecommerce.orderspayments.application.dtos.PaymentSessionDto;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;


public interface PaymentProvider {

    PaymentSessionDto initiatePayment(Order order);

    void cancelPayment(String sessionId);
}

