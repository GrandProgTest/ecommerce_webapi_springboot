package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderItem;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources.OrderItemResource;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources.OrderResource;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources.PaginatedOrderResponse;
import com.finalproject.ecommerce.ecommerce.shared.interfaces.rest.resources.PageMetadata;
import org.springframework.data.domain.Page;

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
                order.getDeliveryStatus() != null ? order.getDeliveryStatus().getName() : null,
                order.getTotalAmount(),
                order.getStripeClientSecret(),
                order.getItems().stream()
                        .map(OrderResourceFromEntityAssembler::toItemResource)
                        .collect(Collectors.toList()),
                order.getCreatedAt(),
                order.getPaidAt()
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

    public static PaginatedOrderResponse toPaginatedResponse(Page<Order> orderPage) {
        PageMetadata pageMetadata = new PageMetadata(
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.hasNext(),
                orderPage.hasPrevious()
        );

        return new PaginatedOrderResponse(
                orderPage.getContent().stream()
                        .map(OrderResourceFromEntityAssembler::toResourceFromEntity)
                        .collect(Collectors.toList()),
                pageMetadata
        );
    }
}
