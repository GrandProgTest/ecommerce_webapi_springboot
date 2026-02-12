package com.finalproject.ecommerce.ecommerce.products.domain.model.queries;

import java.util.List;


public record GetProductsByIdsQuery(List<Long> productIds) {
}
