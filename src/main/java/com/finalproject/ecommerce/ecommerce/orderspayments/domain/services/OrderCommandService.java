package com.finalproject.ecommerce.ecommerce.orderspayments.domain.services;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.*;

public interface OrderCommandService {
    Order handle(CreateOrderFromCartCommand command);

    Order handle(CancelOrderCommand command);

    Order handle(MarkOrderAsPaidCommand command);

    void handle(SeedOrderStatusCommand command);

    void handle(SeedDeliveryStatusCommand command);

    Order handle(UpdateOrderDeliveryStatusCommand command);
}
