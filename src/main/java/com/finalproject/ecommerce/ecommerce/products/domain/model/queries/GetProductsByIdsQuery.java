package com.finalproject.ecommerce.ecommerce.products.domain.model.queries;

import java.util.List;

/**
 * Query to get products by multiple IDs in a single database query (to avoid N+1 problem)
 */
public record GetProductsByIdsQuery(List<Long> productIds) {
}
