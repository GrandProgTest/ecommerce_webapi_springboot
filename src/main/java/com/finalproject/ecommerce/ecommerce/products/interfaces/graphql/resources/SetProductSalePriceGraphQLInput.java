package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources;

import java.time.Instant;

public record SetProductSalePriceGraphQLInput(Double salePrice, Instant salePriceExpireDate) {
}
