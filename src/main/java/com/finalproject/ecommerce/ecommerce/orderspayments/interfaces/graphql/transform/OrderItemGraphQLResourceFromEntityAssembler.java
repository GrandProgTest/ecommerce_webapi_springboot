package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderItem;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.resources.OrderItemGraphQLResource;

public class OrderItemGraphQLResourceFromEntityAssembler {

    public static OrderItemGraphQLResource toResourceFromEntity(OrderItem orderItem) {
        return new OrderItemGraphQLResource(
                orderItem.getId().toString(),
                orderItem.getOrder().getId().toString(),
                orderItem.getProductId().toString(),
                orderItem.getPriceAtPurchase().doubleValue(),
                orderItem.getQuantity()
        );
    }
}
