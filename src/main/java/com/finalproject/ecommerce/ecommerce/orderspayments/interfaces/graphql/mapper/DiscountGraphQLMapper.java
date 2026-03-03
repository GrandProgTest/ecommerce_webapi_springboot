package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.mapper;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CreateDiscountCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.ToggleDiscountStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class DiscountGraphQLMapper {

    public record DiscountGraphQLResource(Long id, String code, Integer percentage, Instant startDate, Instant endDate,
                                          Boolean isActive, Boolean isValid, Instant createdAt) {
    }

    public record CreateDiscountGraphQLInput(String code, Integer percentage, Instant startDate, Instant endDate) {
    }

    public record ToggleDiscountStatusGraphQLResponse(
            Long discountId,
            String code,
            Boolean isActive,
            String message
    ) {
    }

    public static DiscountGraphQLResource toResource(Discount discount) {
        return new DiscountGraphQLResource(
                discount.getId(), discount.getCode(), discount.getPercentage(),
                discount.getStartDate(), discount.getEndDate(), discount.getIsActive(),
                discount.isValid(), discount.getCreatedAt()
        );
    }

    public static List<DiscountGraphQLResource> toResourceList(List<Discount> discounts) {
        return discounts.stream()
                .map(DiscountGraphQLMapper::toResource)
                .collect(Collectors.toList());
    }

    public static CreateDiscountCommand toCreateCommand(CreateDiscountGraphQLInput input) {
        return new CreateDiscountCommand(input.code(), input.percentage(), input.startDate(), input.endDate());
    }

    public static ToggleDiscountStatusCommand toToggleCommand(Long discountId) {
        return new ToggleDiscountStatusCommand(discountId);
    }
}

