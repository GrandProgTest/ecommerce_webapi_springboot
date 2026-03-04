package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetUserByIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetAllOrdersWithPaginationQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetOrderByIdQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetUserOrdersWithPaginationQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.mapper.OrderGraphQLMapper;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.mapper.OrderGraphQLMapper.OrderGraphQLResource;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.mapper.OrderGraphQLMapper.OrderUserGraphQLResource;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OrderQueryResolver {

    private final OrderQueryService orderQueryService;
    private final UserQueryService userQueryService;

    public OrderQueryResolver (OrderQueryService orderQueryService, UserQueryService userQueryService) {
        this.orderQueryService = orderQueryService;
        this.userQueryService = userQueryService;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public OrderGraphQLResource getOrderById(@Argument String id) {
        return orderQueryService.handle(new GetOrderByIdQuery(Long.parseLong(id)))
                .map(OrderGraphQLMapper::toResource)
                .orElse(null);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<OrderGraphQLResource> getUserOrders(@Argument String userId, @Argument Integer page, @Argument Integer size, @Argument String sortBy, @Argument String sortDirection) {
        return orderQueryService.handle(new GetUserOrdersWithPaginationQuery(Long.parseLong(userId), page, size, sortBy, sortDirection))
                .getContent().stream().map(OrderGraphQLMapper::toResource).collect(Collectors.toList());
    }

    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
    public List<OrderGraphQLResource> getAllOrders(@Argument Integer page, @Argument Integer size, @Argument String sortBy, @Argument String sortDirection, @Argument String status, @Argument String deliveryStatus, @Argument String userId, @Argument String dateFrom, @Argument String dateTo) {
        Long parsedUserId = userId != null ? Long.parseLong(userId) : null;
        Instant parsedDateFrom = dateFrom != null ? Instant.parse(dateFrom) : null;
        Instant parsedDateTo = dateTo != null ? Instant.parse(dateTo) : null;
        return orderQueryService.handle(new GetAllOrdersWithPaginationQuery(page, size, sortBy, sortDirection, status, deliveryStatus, parsedUserId, parsedDateFrom, parsedDateTo))
                .getContent().stream().map(OrderGraphQLMapper::toResource).collect(Collectors.toList());
    }

    @SchemaMapping(typeName = "Order", field = "user")
    public OrderUserGraphQLResource user(OrderGraphQLResource order) {
        return userQueryService.handle(new GetUserByIdQuery(Long.parseLong(order.userId())))
                .map(OrderGraphQLMapper::toResource)
                .orElse(null);
    }
}
