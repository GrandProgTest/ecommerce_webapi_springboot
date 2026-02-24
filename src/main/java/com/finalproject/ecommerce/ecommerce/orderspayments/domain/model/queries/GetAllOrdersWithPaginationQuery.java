package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries;

import java.time.Instant;

public record GetAllOrdersWithPaginationQuery(int page, int size, String sortBy, String sortDirection, String status,
                                              String deliveryStatus, Long userId,
                                              Instant dateFrom, Instant dateTo) {
    public GetAllOrdersWithPaginationQuery {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be non-negative");
        }
        if (size != 20 && size != 50 && size != 100) {
            throw new IllegalArgumentException("Size must be 20, 50, or 100");
        }
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom must be before dateTo");
        }
    }
}

