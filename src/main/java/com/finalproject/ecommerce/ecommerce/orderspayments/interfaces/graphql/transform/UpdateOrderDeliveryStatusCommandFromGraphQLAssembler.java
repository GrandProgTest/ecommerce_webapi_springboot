package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.UpdateOrderDeliveryStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.DeliveryStatuses;

public class UpdateOrderDeliveryStatusCommandFromGraphQLAssembler {

    public static UpdateOrderDeliveryStatusCommand toCommandFromArguments(
            String orderId,
            String deliveryStatus) {

        Long parsedOrderId = Long.parseLong(orderId);
        DeliveryStatuses newDeliveryStatus = DeliveryStatuses.valueOf(deliveryStatus);

        return new UpdateOrderDeliveryStatusCommand(parsedOrderId, newDeliveryStatus);
    }
}

