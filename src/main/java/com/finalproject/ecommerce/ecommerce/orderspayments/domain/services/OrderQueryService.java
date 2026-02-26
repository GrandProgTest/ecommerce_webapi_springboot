package com.finalproject.ecommerce.ecommerce.orderspayments.domain.services;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.*;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface OrderQueryService {

    Optional<Order> handle(GetOrderByIdQuery query);

    Page<Order> handle(GetAllOrdersWithPaginationQuery query);

    Page<Order> handle(GetUserOrdersWithPaginationQuery query);
}
