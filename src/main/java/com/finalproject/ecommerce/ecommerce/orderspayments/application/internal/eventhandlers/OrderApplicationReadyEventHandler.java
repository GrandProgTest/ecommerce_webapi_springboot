package com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.eventhandlers;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.SeedDeliveryStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.SeedOrderStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Slf4j
@Service
public class OrderApplicationReadyEventHandler {

    private final OrderCommandService orderCommandService;

    public OrderApplicationReadyEventHandler(OrderCommandService orderCommandService) {
        this.orderCommandService = orderCommandService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        var applicationName = event.getApplicationContext().getId();
        log.info("Starting to verify if order status seeding is needed for {} at {}", applicationName, currentTimestamp());
        var seedOrderStatusCommand = new SeedOrderStatusCommand();
        orderCommandService.handle(seedOrderStatusCommand);
        log.info("Order status seeding verification finished for {} at {}", applicationName, currentTimestamp());

        log.info("Starting to verify if delivery status seeding is needed for {} at {}", applicationName, currentTimestamp());
        var seedDeliveryStatusCommand = new SeedDeliveryStatusCommand();
        orderCommandService.handle(seedDeliveryStatusCommand);
        log.info("Delivery status seeding verification finished for {} at {}", applicationName, currentTimestamp());
    }

    private Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
