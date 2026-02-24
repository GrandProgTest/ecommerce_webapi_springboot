package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.mapper.OrderGraphQLMapper;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.mapper.OrderGraphQLMapper.OrderGraphQLResource;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class OrderMutationResolver {

    private final OrderCommandService orderCommandService;

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public OrderGraphQLResource createOrder(@Argument String userId, @Argument String cartId, @Argument String addressId, @Argument String discountCode) {
        var command = OrderGraphQLMapper.toCreateOrderCommand(userId, cartId, addressId, discountCode);
        var order = orderCommandService.handle(command);
        return OrderGraphQLMapper.toResource(order);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public OrderGraphQLResource cancelOrder(@Argument String orderId) {
        var command = OrderGraphQLMapper.toCancelCommand(orderId);
        return OrderGraphQLMapper.toResource(orderCommandService.handle(command));
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public OrderGraphQLResource updateOrderDeliveryStatus(@Argument String orderId, @Argument String deliveryStatus) {
        var command = OrderGraphQLMapper.toUpdateDeliveryCommand(orderId, deliveryStatus);
        return OrderGraphQLMapper.toResource(orderCommandService.handle(command));
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public OrderGraphQLResource confirmPayment(@Argument String orderId, @Argument String paymentMethodId) {
        var command = OrderGraphQLMapper.toConfirmPaymentCommand(orderId, paymentMethodId);
        return OrderGraphQLMapper.toResource(orderCommandService.handle(command));
    }
}
