package com.finalproject.ecommerce.ecommerce.products.application.dto;

public record PageMetadataResponse(int currentPage, int pageSize, long totalElements, int totalPages, boolean hasNext,
                                   boolean hasPrevious) {
}