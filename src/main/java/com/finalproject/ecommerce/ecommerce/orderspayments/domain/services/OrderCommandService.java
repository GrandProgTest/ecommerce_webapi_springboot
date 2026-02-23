package com.finalproject.ecommerce.ecommerce.orderspayments.domain.services;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.*;

public interface OrderCommandService {
    Order handle(CreateOrderFromCartCommand command);

    Order handle(CancelOrderCommand command);

    Order handle(MarkOrderAsPaidCommand command);

    Order handle(ConfirmPaymentCommand command);

    void handle(SeedOrderStatusCommand command);

    void handle(SeedDeliveryStatusCommand command);

    void handle(SeedPaymentIntentStatusCommand command);

    Order handle(UpdateOrderDeliveryStatusCommand command);
}
