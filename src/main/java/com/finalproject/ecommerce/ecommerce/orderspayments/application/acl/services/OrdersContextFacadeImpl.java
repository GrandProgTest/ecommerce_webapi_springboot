package com.finalproject.ecommerce.ecommerce.orderspayments.application.acl.services;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.OrderStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories.OrderRepository;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.acl.OrdersContextFacade;
import org.springframework.stereotype.Service;

@Service
public class OrdersContextFacadeImpl implements OrdersContextFacade {

    private final OrderRepository orderRepository;

    public OrdersContextFacadeImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public boolean productExistsInOrders(Long productId) {
        return orderRepository.existsByItemsProductIdAndStatusName(
            productId,
            OrderStatuses.PENDING.name()
        );
    }
}

