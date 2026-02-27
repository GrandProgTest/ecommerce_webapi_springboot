package com.finalproject.ecommerce.ecommerce.products.application.dto;

import java.util.List;

public record ProductPageResponse(List<ProductResponse> products, PageMetadataResponse pageMetadata) {
}