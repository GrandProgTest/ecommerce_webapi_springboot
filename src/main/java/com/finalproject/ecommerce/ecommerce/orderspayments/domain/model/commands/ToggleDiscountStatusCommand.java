package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands;

public record ToggleDiscountStatusCommand(Long discountId) {
    public ToggleDiscountStatusCommand {
        if (discountId == null) {
            throw new IllegalArgumentException("Discount ID cannot be null");
        }
    }
}

