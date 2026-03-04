package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetDiscountByIdQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.DiscountCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.DiscountQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.mapper.DiscountGraphQLMapper;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.mapper.DiscountGraphQLMapper.CreateDiscountGraphQLInput;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.mapper.DiscountGraphQLMapper.DiscountGraphQLResource;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.mapper.DiscountGraphQLMapper.ToggleDiscountStatusGraphQLResponse;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class DiscountMutationResolver {

    private final DiscountCommandService discountCommandService;
    private final DiscountQueryService discountQueryService;

    public DiscountMutationResolver(DiscountCommandService discountCommandService, DiscountQueryService discountQueryService) {
        this.discountCommandService = discountCommandService;
        this.discountQueryService = discountQueryService;
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public DiscountGraphQLResource createDiscount(@Argument CreateDiscountGraphQLInput input) {
        var command = DiscountGraphQLMapper.toCreateCommand(input);
        Discount discount = discountCommandService.handle(command);
        return DiscountGraphQLMapper.toResource(discount);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ToggleDiscountStatusGraphQLResponse toggleDiscountStatus(@Argument Long discountId) {
        var command = DiscountGraphQLMapper.toToggleCommand(discountId);
        boolean isActive = discountCommandService.handle(command);
        var discount = discountQueryService.handle(new GetDiscountByIdQuery(discountId))
                .orElseThrow(() -> new IllegalArgumentException("Discount not found with ID: " + discountId));

        String message = isActive
                ? "Discount activated successfully"
                : "Discount deactivated successfully";

        return new ToggleDiscountStatusGraphQLResponse(
                discount.getId(),
                discount.getCode(),
                discount.getIsActive(),
                message
        );
    }
}

