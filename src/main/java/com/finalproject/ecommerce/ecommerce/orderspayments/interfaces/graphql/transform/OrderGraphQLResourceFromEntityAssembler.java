package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.resources.*;

import java.util.stream.Collectors;

public class OrderGraphQLResourceFromEntityAssembler {

    public static OrderGraphQLResource toResourceFromEntity(Order order) {
        return new OrderGraphQLResource(
                order.getId().toString(),
                order.getUserId().toString(),
                null,
                order.getCartId() != null ? order.getCartId().toString() : null,
                order.getAddressId().toString(),
                order.getDiscount() != null ? order.getDiscount().getCode() : null,
                order.getStatus().getName(),
                order.getDeliveryStatus() != null ? order.getDeliveryStatus().getName() : null,
                order.getTotalAmount().doubleValue(),
                order.getStripeClientSecret(),
                order.getItems().stream()
                        .map(OrderItemGraphQLResourceFromEntityAssembler::toResourceFromEntity)
                        .collect(Collectors.toList()),
                order.getCreatedAt(),
                order.getPaidAt()
        );
    }
}

