package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands;

import java.time.Instant;

public record CreateDiscountCommand(
        String code,
        Integer percentage,
        Instant startDate,
        Instant endDate
) {
    public CreateDiscountCommand {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Discount code cannot be empty");
        }
        if (percentage == null || percentage <= 0 || percentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 1 and 100");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
}

