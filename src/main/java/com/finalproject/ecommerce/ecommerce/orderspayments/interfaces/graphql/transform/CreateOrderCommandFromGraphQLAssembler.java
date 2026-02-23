package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CreateOrderFromCartCommand;

public class CreateOrderCommandFromGraphQLAssembler {

    public static CreateOrderFromCartCommand toCommandFromArguments(
            String userId,
            String cartId,
            String addressId,
            String discountCode) {

        Long parsedUserId = Long.parseLong(userId);
        Long parsedCartId = Long.parseLong(cartId);
        Long parsedAddressId = Long.parseLong(addressId);

        return new CreateOrderFromCartCommand(
                parsedUserId,
                parsedCartId,
                parsedAddressId,
                discountCode
        );
    }
}

