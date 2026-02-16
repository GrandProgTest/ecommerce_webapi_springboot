package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.UpdateOrderStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.OrderStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources.UpdateOrderStatusResource;

public class UpdateOrderStatusCommandFromResourceAssembler {

    public static UpdateOrderStatusCommand toCommandFromResource(Long orderId, UpdateOrderStatusResource resource) {
        var orderStatus = OrderStatuses.valueOf(resource.status().toUpperCase());
        return new UpdateOrderStatusCommand(orderId, orderStatus);
    }
}

