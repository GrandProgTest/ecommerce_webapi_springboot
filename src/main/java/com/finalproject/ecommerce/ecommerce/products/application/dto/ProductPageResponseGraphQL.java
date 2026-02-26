package com.finalproject.ecommerce.ecommerce.products.application.dto;

import java.util.List;

public record ProductPageResponseGraphQL(List<ProductResponseGraphQL> products,
                                         PageMetadataResponse pageMetadata) {
}

