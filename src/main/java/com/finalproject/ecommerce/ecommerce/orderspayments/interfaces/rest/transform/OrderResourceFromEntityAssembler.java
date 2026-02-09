package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderItem;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources.OrderItemResource;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources.OrderResource;

import java.util.stream.Collectors;

public class OrderResourceFromEntityAssembler {

    public static OrderResource toResourceFromEntity(Order order) {
        return new OrderResource(
                order.getId(),
                order.getUserId(),
                order.getCartId(),
                order.getAddressId(),
                order.getDiscount() != null ? order.getDiscount().getCode() : null,
                order.getStatus().getName(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getItems().stream()
                        .map(OrderResourceFromEntityAssembler::toItemResource)
                        .collect(Collectors.toList()),
                order.getCreatedAt()
        );
    }

    private static OrderItemResource toItemResource(OrderItem item) {
        return new OrderItemResource(
                item.getId(),
                item.getProductId(),
                item.getPriceAtPurchase(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}
