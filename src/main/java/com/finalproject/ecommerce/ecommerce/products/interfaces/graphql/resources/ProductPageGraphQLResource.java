package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources;

import java.util.List;

public record ProductPageGraphQLResource(
        List<ProductGraphQLResource> products,
        PageMetadataGraphQLResource pageMetadata
) {
}

