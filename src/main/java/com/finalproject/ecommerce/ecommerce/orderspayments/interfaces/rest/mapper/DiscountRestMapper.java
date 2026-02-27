package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.mapper;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CreateDiscountCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.ToggleDiscountStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class DiscountRestMapper {

    public record DiscountResource(Long id, String code, Integer percentage, Instant startDate, Instant endDate,
                                   Boolean isActive, Boolean isValid, Instant createdAt) {
    }

    public record CreateDiscountResource(String code, Integer percentage, Instant startDate, Instant endDate) {
    }

    public record ToggleDiscountStatusResponse(
            Long discountId,
            String code,
            Boolean isActive,
            String message
    ) {
    }

    public static ToggleDiscountStatusCommand toToggleCommand(Long discountId) {
        return new ToggleDiscountStatusCommand(discountId);
    }

    public static DiscountResource toResource(Discount discount) {
        return new DiscountResource(discount.getId(), discount.getCode(), discount.getPercentage(), discount.getStartDate(), discount.getEndDate(), discount.getIsActive(), discount.isValid(), discount.getCreatedAt());
    }

    public static List<DiscountResource> toResourceList(List<Discount> discounts) {
        return discounts.stream().map(DiscountRestMapper::toResource).collect(Collectors.toList());
    }

    public static CreateDiscountCommand toCreateCommand(CreateDiscountResource resource) {
        return new CreateDiscountCommand(resource.code(), resource.percentage(), resource.startDate(), resource.endDate());
    }
}

