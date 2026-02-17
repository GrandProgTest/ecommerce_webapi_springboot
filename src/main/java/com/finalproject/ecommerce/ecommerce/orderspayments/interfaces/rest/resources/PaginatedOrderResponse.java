package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources;

import com.finalproject.ecommerce.ecommerce.shared.interfaces.rest.resources.PageMetadata;

import java.util.List;

public record PaginatedOrderResponse(List<OrderResource> orders, PageMetadata pageMetadata) {
}

