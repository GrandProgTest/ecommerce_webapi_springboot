package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetAllDiscountsQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetDiscountByCodeQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetDiscountByIdQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.DiscountQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.mapper.DiscountGraphQLMapper;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.mapper.DiscountGraphQLMapper.DiscountGraphQLResource;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DiscountQueryResolver {

    private final DiscountQueryService discountQueryService;

    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
    public List<DiscountGraphQLResource> getAllDiscounts() {
        return discountQueryService.handle(new GetAllDiscountsQuery()).stream()
                .map(DiscountGraphQLMapper::toResource)
                .collect(Collectors.toList());
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public DiscountGraphQLResource getDiscountByCode(@Argument String code) {
        return discountQueryService.handle(new GetDiscountByCodeQuery(code))
                .map(DiscountGraphQLMapper::toResource)
                .orElse(null);
    }

    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
    public DiscountGraphQLResource getDiscountById(@Argument Long id) {
        return discountQueryService.handle(new GetDiscountByIdQuery(id))
                .map(DiscountGraphQLMapper::toResource)
                .orElse(null);
    }
}

