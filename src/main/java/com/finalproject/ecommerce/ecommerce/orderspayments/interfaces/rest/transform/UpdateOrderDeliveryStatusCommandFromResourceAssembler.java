package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.UpdateOrderDeliveryStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.DeliveryStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources.UpdateOrderDeliveryStatusResource;

public class UpdateOrderDeliveryStatusCommandFromResourceAssembler {

    public static UpdateOrderDeliveryStatusCommand toCommandFromResource(Long orderId, UpdateOrderDeliveryStatusResource resource) {
        var deliveryStatus = DeliveryStatuses.valueOf(resource.deliveryStatus().toUpperCase());
        return new UpdateOrderDeliveryStatusCommand(orderId, deliveryStatus);
    }
}

