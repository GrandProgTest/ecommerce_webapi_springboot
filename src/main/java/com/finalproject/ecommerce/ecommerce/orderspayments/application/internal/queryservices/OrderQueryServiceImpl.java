package com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.*;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories.OrderRepository;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.specifications.OrderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
    public Optional<Order> handle(GetOrderByIdQuery query) {
        Optional<Order> order = orderRepository.findById(query.orderId());
        order.ifPresent(o -> iamContextFacade.validateManagerOrUserCanAccessResource(o.getUserId()));
        return order;
    }

    @Override
    public Page<Order> handle(GetAllOrdersWithPaginationQuery query) {
        Sort sort = query.sortDirection().equalsIgnoreCase("desc")
                ? Sort.by(query.sortBy()).descending()
                : Sort.by(query.sortBy()).ascending();

        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);

        return orderRepository.findAll(
                OrderSpecification.withFilters(
                        query.status(),
                        query.deliveryStatus(),
                        query.userId(),
                        query.dateFrom(),
                        query.dateTo()
                ),
                pageable
        );
    }

    @Override
    public Page<Order> handle(GetUserOrdersWithPaginationQuery query) {
        iamContextFacade.validateManagerOrUserCanAccessResource(query.userId());

        Sort sort = query.sortDirection().equalsIgnoreCase("desc")
                ? Sort.by(query.sortBy()).descending()
                : Sort.by(query.sortBy()).ascending();

        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);

        return orderRepository.findByUserId(query.userId(), pageable);
    }
}
