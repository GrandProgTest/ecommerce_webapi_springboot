package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.resources.*;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.transform.*;
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
    public OrderGraphQLResource createOrder(
            @Argument String userId,
            @Argument String cartId,
            @Argument String addressId,
            @Argument String discountCode) {

        var command = CreateOrderCommandFromGraphQLAssembler.toCommandFromArguments(
                userId, cartId, addressId, discountCode
        );
        var order = orderCommandService.handle(command);

        return OrderGraphQLResourceFromEntityAssembler.toResourceFromEntity(order);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public OrderGraphQLResource cancelOrder(@Argument String orderId) {
        var command = CancelOrderCommandFromGraphQLAssembler.toCommandFromArguments(orderId);
        var cancelledOrder = orderCommandService.handle(command);

        return OrderGraphQLResourceFromEntityAssembler.toResourceFromEntity(cancelledOrder);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public OrderGraphQLResource updateOrderDeliveryStatus(
            @Argument String orderId,
            @Argument String deliveryStatus) {

        var command = UpdateOrderDeliveryStatusCommandFromGraphQLAssembler.toCommandFromArguments(
                orderId, deliveryStatus
        );
        var updatedOrder = orderCommandService.handle(command);

        return OrderGraphQLResourceFromEntityAssembler.toResourceFromEntity(updatedOrder);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public OrderGraphQLResource confirmPayment(
            @Argument String orderId,
            @Argument String paymentMethodId) {

        var command = ConfirmPaymentCommandFromGraphQLAssembler.toCommandFromArguments(
                orderId, paymentMethodId
        );
        var paidOrder = orderCommandService.handle(command);

        return OrderGraphQLResourceFromEntityAssembler.toResourceFromEntity(paidOrder);
    }
}
