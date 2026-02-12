package com.finalproject.ecommerce.ecommerce.orderspayments.domain.services;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CancelOrderCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CreateOrderFromCartCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.MarkOrderAsPaidCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.SeedOrderStatusCommand;

public interface OrderCommandService {
    Order handle(CreateOrderFromCartCommand command);

    Order handle(CancelOrderCommand command);

    Order handle(MarkOrderAsPaidCommand command);

    void handle(SeedOrderStatusCommand command);
}
