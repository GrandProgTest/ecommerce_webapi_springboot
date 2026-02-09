package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;

import com.finalproject.ecommerce.ecommerce.shared.interfaces.rest.resources.PageMetadata;

import java.util.List;

public record PaginatedProductResponse(List<ProductResource> products, PageMetadata pageMetadata) {
}
