package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.ConfirmPaymentCommand;

public class ConfirmPaymentCommandFromGraphQLAssembler {

    private static final String DEFAULT_PAYMENT_METHOD_ID = "pm_card_visa";

    public static ConfirmPaymentCommand toCommandFromArguments(String orderId, String paymentMethodId) {
        Long parsedOrderId = Long.parseLong(orderId);

        String methodId = (paymentMethodId != null && !paymentMethodId.isBlank())
                ? paymentMethodId
                : DEFAULT_PAYMENT_METHOD_ID;

        return new ConfirmPaymentCommand(parsedOrderId, methodId);
    }
}

