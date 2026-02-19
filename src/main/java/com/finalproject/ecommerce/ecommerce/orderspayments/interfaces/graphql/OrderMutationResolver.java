package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CancelOrderCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CreateOrderFromCartCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.UpdateOrderDeliveryStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.DeliveryStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.resources.OrderGraphQLResource;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.transform.OrderGraphQLResourceFromEntityAssembler;
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

        Long parsedUserId = Long.parseLong(userId);
        Long parsedCartId = Long.parseLong(cartId);
        Long parsedAddressId = Long.parseLong(addressId);

        var command = new CreateOrderFromCartCommand(
                parsedUserId,
                parsedCartId,
                parsedAddressId,
                discountCode
        );
        var order = orderCommandService.handle(command);

        return OrderGraphQLResourceFromEntityAssembler.toResourceFromEntity(order);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public OrderGraphQLResource cancelOrder(@Argument String orderId) {
        Long parsedOrderId = Long.parseLong(orderId);
        var command = new CancelOrderCommand(parsedOrderId);
        var cancelledOrder = orderCommandService.handle(command);

        return OrderGraphQLResourceFromEntityAssembler.toResourceFromEntity(cancelledOrder);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public OrderGraphQLResource updateOrderDeliveryStatus(
            @Argument String orderId,
            @Argument String deliveryStatus) {

        Long parsedOrderId = Long.parseLong(orderId);
        DeliveryStatuses newDeliveryStatus = DeliveryStatuses.valueOf(deliveryStatus);

        var command = new UpdateOrderDeliveryStatusCommand(parsedOrderId, newDeliveryStatus);
        var updatedOrder = orderCommandService.handle(command);

        return OrderGraphQLResourceFromEntityAssembler.toResourceFromEntity(updatedOrder);
    }
}
