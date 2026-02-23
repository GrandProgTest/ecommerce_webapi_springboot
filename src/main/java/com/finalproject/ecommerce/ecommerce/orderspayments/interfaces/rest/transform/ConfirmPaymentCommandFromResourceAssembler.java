package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.ConfirmPaymentCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources.ConfirmPaymentResource;

public class ConfirmPaymentCommandFromResourceAssembler {

    private static final String DEFAULT_PAYMENT_METHOD_ID = "pm_card_visa";

    public static ConfirmPaymentCommand toCommandFromResource(Long orderId, ConfirmPaymentResource resource) {
        String paymentMethodId = (resource != null && resource.paymentMethodId() != null && !resource.paymentMethodId().isBlank())
                ? resource.paymentMethodId()
                : DEFAULT_PAYMENT_METHOD_ID;

        return new ConfirmPaymentCommand(orderId, paymentMethodId);
    }
}

