package com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.eventhandlers;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.SeedOrderStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class OrderApplicationReadyEventHandler {

    private final OrderCommandService orderCommandService;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderApplicationReadyEventHandler.class);

    public OrderApplicationReadyEventHandler(OrderCommandService orderCommandService) {
        this.orderCommandService = orderCommandService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        var applicationName = event.getApplicationContext().getId();
        LOGGER.info("Starting to verify if order status seeding is needed for {} at {}", applicationName, currentTimestamp());
        var seedOrderStatusCommand = new SeedOrderStatusCommand();
        orderCommandService.handle(seedOrderStatusCommand);
        LOGGER.info("Order status seeding verification finished for {} at {}", applicationName, currentTimestamp());
    }

    private Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
