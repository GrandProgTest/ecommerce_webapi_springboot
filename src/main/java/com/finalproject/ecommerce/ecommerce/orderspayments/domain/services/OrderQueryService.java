package com.finalproject.ecommerce.ecommerce.orderspayments.domain.services;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetAllOrdersQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetOrderByIdQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetOrdersByUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface OrderQueryService {
    List<Order> handle(GetAllOrdersQuery query);

    List<Order> handle(GetOrdersByUserIdQuery query);

    Optional<Order> handle(GetOrderByIdQuery query);
}
