package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CreateOrderFromCartCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources.CreateOrderResource;

public class CreateOrderCommandFromResourceAssembler {

    public static CreateOrderFromCartCommand toCommandFromResource(Long userId, Long cartId, CreateOrderResource resource) {
        return new CreateOrderFromCartCommand(
                userId,
                cartId,
                resource.addressId(),
                resource.discountCode()
        );
    }
}
