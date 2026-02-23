package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetUserByIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetAllOrdersWithPaginationQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetOrderByIdQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetUserOrdersWithPaginationQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.resources.OrderGraphQLResource;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.resources.OrderUserGraphQLResource;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.transform.OrderGraphQLResourceFromEntityAssembler;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.transform.OrderUserGraphQLResourceFromEntityAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class OrderQueryResolver {

    private final OrderQueryService orderQueryService;
    private final UserQueryService userQueryService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public OrderGraphQLResource order(@Argument String id) {
        Long orderId = Long.parseLong(id);
        var query = new GetOrderByIdQuery(orderId);
        return orderQueryService.handle(query)
                .map(OrderGraphQLResourceFromEntityAssembler::toResourceFromEntity)
                .orElse(null);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<OrderGraphQLResource> getUserOrders(
            @Argument String userId,
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sortBy,
            @Argument String sortDirection) {

        Long parsedUserId = Long.parseLong(userId);

        return orderQueryService.handle(
                        new GetUserOrdersWithPaginationQuery(parsedUserId, page, size, sortBy, sortDirection))
                .getContent()
                .stream()
                .map(OrderGraphQLResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
    }

    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
    public List<OrderGraphQLResource> getAllOrders(
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sortBy,
            @Argument String sortDirection,
            @Argument String status,
            @Argument String deliveryStatus,
            @Argument String userId) {

        Long parsedUserId = userId != null ? Long.parseLong(userId) : null;

        return orderQueryService.handle(
                        new GetAllOrdersWithPaginationQuery(
                                page, size, sortBy, sortDirection, status, deliveryStatus, parsedUserId))
                .getContent()
                .stream()
                .map(OrderGraphQLResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
    }

    @SchemaMapping(typeName = "Order", field = "user")
    public OrderUserGraphQLResource user(OrderGraphQLResource order) {
        Long userId = Long.parseLong(order.userId());
        return userQueryService.handle(new GetUserByIdQuery(userId))
                .map(OrderUserGraphQLResourceFromEntityAssembler::toResourceFromEntity)
                .orElse(null);
    }
}

