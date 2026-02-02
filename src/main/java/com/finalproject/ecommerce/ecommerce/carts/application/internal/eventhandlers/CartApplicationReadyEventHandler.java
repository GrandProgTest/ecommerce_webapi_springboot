package com.finalproject.ecommerce.ecommerce.carts.application.internal.eventhandlers;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.SeedCartStatusCommand;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class CartApplicationReadyEventHandler {

    private final CartCommandService cartCommandService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CartApplicationReadyEventHandler.class);

    public CartApplicationReadyEventHandler(CartCommandService cartCommandService) {
        this.cartCommandService = cartCommandService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        var applicationName = event.getApplicationContext().getId();
        LOGGER.info("Starting to verify if cart status seeding is needed for {} at {}", applicationName, currentTimestamp());
        var seedCartStatusCommand = new SeedCartStatusCommand();
        cartCommandService.handle(seedCartStatusCommand);
        LOGGER.info("Cart status seeding verification finished for {} at {}", applicationName, currentTimestamp());
    }

    private Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
