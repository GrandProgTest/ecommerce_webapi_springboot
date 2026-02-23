package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CancelOrderCommand;

public class CancelOrderCommandFromGraphQLAssembler {

    public static CancelOrderCommand toCommandFromArguments(String orderId) {
        Long parsedOrderId = Long.parseLong(orderId);
        return new CancelOrderCommand(parsedOrderId);
    }
}
