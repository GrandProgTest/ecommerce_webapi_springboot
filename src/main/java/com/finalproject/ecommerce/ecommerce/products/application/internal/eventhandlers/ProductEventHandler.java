package com.finalproject.ecommerce.ecommerce.products.application.internal.eventhandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class ProductEventHandler {

    public ProductEventHandler()
    {}

    public void onStockLow(Long productId, Integer currentStock) {
        log.warn("Stock low for product {}: {} units remaining", productId, currentStock);
    }
}
