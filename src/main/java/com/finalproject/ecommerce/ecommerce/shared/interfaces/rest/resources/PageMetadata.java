package com.finalproject.ecommerce.ecommerce.shared.interfaces.rest.resources;

public record PageMetadata(int currentPage, int pageSize, long totalElements, int totalPages, boolean hasNext,
                           boolean hasPrevious) {
}
