package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.mapper;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderItem;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.DeliveryStatuses;
import com.finalproject.ecommerce.ecommerce.shared.interfaces.rest.resources.PageMetadata;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class OrderRestMapper {


    public record OrderResource(Long id, Long userId, Long cartId, Long addressId, String discountCode, String status,
                                String deliveryStatus, BigDecimal totalAmount, String clientSecret,
                                List<OrderItemResource> items, Instant createdAt, Instant paidAt) {
    }

    public record OrderItemResource(Long id, Long productId, BigDecimal priceAtPurchase, Integer quantity, BigDecimal subtotal) {
    }

    public record PaginatedOrderResponse(List<OrderResource> orders, PageMetadata pageMetadata) {
    }

    public record CreateOrderResource(Long cartId, Long addressId, String discountCode) {
    }

    public record UpdateOrderDeliveryStatusResource(String deliveryStatus) {
    }

    public record ConfirmPaymentResource(String paymentMethodId) {
    }

    public static OrderResource toResource(Order order) {
        return new OrderResource(order.getId(), order.getUserId(), order.getCartId(), order.getAddressId(),
                order.getDiscount() != null ? order.getDiscount().getCode() : null,
                order.getStatus().getName(),
                order.getDeliveryStatus() != null ? order.getDeliveryStatus().getName() : null,
                order.getTotalAmount(), order.getStripeClientSecret(),
                order.getItems().stream().map(OrderRestMapper::toResource).collect(Collectors.toList()),
                order.getCreatedAt(), order.getPaidAt());
    }

    public static OrderItemResource toResource(OrderItem item) {
        return new OrderItemResource(item.getId(), item.getProductId(), item.getPriceAtPurchase(), item.getQuantity(), item.getSubtotal());
    }

    public static PaginatedOrderResponse toPaginatedResponse(Page<Order> orderPage) {
        var pageMetadata = new PageMetadata(orderPage.getNumber(), orderPage.getSize(), orderPage.getTotalElements(), orderPage.getTotalPages(), orderPage.hasNext(), orderPage.hasPrevious());
        return new PaginatedOrderResponse(orderPage.getContent().stream().map(OrderRestMapper::toResource).collect(Collectors.toList()), pageMetadata);
    }

    public static CreateOrderFromCartCommand toCreateCommand(Long userId, CreateOrderResource r) {
        return new CreateOrderFromCartCommand(userId, r.cartId(), r.addressId(), r.discountCode());
    }

    public static UpdateOrderDeliveryStatusCommand toUpdateDeliveryCommand(Long orderId, UpdateOrderDeliveryStatusResource r) {
        return new UpdateOrderDeliveryStatusCommand(orderId, DeliveryStatuses.valueOf(r.deliveryStatus().toUpperCase()));
    }

    public static ConfirmPaymentCommand toConfirmPaymentCommand(Long orderId, ConfirmPaymentResource r) {
        String methodId = (r != null && r.paymentMethodId() != null && !r.paymentMethodId().isBlank()) ? r.paymentMethodId() : "pm_card_visa";
        return new ConfirmPaymentCommand(orderId, methodId);
    }
}

