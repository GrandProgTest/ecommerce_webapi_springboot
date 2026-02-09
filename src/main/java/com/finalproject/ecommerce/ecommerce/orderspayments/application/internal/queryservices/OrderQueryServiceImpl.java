package com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetAllOrdersQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetOrderByIdQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetOrdersByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final IamContextFacade iamContextFacade;

    public OrderQueryServiceImpl(OrderRepository orderRepository, IamContextFacade iamContextFacade) {
        this.orderRepository = orderRepository;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> handle(GetAllOrdersQuery query) {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> handle(GetOrdersByUserIdQuery query) {
        iamContextFacade.validateUserCanAccessResource(query.userId());
        return orderRepository.findByUserId(query.userId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> handle(GetOrderByIdQuery query) {
        Optional<Order> order = orderRepository.findById(query.orderId());
        order.ifPresent(o -> iamContextFacade.validateUserCanAccessResource(o.getUserId()));
        return order;
    }
}
