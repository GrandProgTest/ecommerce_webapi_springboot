package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.mapper;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderItem;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.DeliveryStatuses;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class OrderGraphQLMapper {

    public record OrderGraphQLResource(String id, String userId, OrderUserGraphQLResource user, String cartId,
                                       String addressId, String discountCode, String status, String deliveryStatus,
                                       Double totalAmount, String clientSecret, List<OrderItemGraphQLResource> items,
                                       Instant createdAt, Instant paidAt) {
    }

    public record OrderItemGraphQLResource(String id, String orderId, String productId, Double priceAtPurchase, Integer quantity) {
    }

    public record OrderUserGraphQLResource(String id, String username, String email) {
    }

    public static OrderGraphQLResource toResource(Order order) {
        return new OrderGraphQLResource(
                order.getId().toString(), order.getUserId().toString(), null,
                order.getCartId() != null ? order.getCartId().toString() : null,
                order.getAddressId().toString(),
                order.getDiscount() != null ? order.getDiscount().getCode() : null,
                order.getStatus().getName(),
                order.getDeliveryStatus() != null ? order.getDeliveryStatus().getName() : null,
                order.getTotalAmount().doubleValue(), order.getStripeClientSecret(),
                order.getItems().stream().map(OrderGraphQLMapper::toResource).collect(Collectors.toList()),
                order.getCreatedAt(), order.getPaidAt()
        );
    }

    public static OrderItemGraphQLResource toResource(OrderItem item) {
        return new OrderItemGraphQLResource(item.getId().toString(), item.getOrder().getId().toString(), item.getProductId().toString(), item.getPriceAtPurchase().doubleValue(), item.getQuantity());
    }

    public static OrderUserGraphQLResource toResource(User user) {
        if (user == null) return null;
        return new OrderUserGraphQLResource(user.getId().toString(), user.getUsername(), user.getEmail());
    }

    public static CreateOrderFromCartCommand toCreateOrderCommand(String userId, String cartId, String addressId, String discountCode) {
        return new CreateOrderFromCartCommand(Long.parseLong(userId), Long.parseLong(cartId), Long.parseLong(addressId), discountCode);
    }

    public static CancelOrderCommand toCancelCommand(String orderId) {
        return new CancelOrderCommand(Long.parseLong(orderId));
    }

    public static UpdateOrderDeliveryStatusCommand toUpdateDeliveryCommand(String orderId, String deliveryStatus) {
        return new UpdateOrderDeliveryStatusCommand(Long.parseLong(orderId), DeliveryStatuses.valueOf(deliveryStatus));
    }

    public static ConfirmPaymentCommand toConfirmPaymentCommand(String orderId, String paymentMethodId) {
        String methodId = (paymentMethodId != null && !paymentMethodId.isBlank()) ? paymentMethodId : "pm_card_visa";
        return new ConfirmPaymentCommand(Long.parseLong(orderId), methodId);
    }
}

