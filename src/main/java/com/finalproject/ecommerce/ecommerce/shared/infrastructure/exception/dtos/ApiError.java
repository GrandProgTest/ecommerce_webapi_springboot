package com.finalproject.ecommerce.ecommerce.shared.infrastructure.exception.dtos;

import java.time.LocalDateTime;

/**
 * Standard API error response format
 * Used across all exception handlers for consistency
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
