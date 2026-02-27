package com.finalproject.ecommerce.ecommerce.shared.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error message", example = "Invalid request or cart is empty")
    private String message;

    @Schema(description = "Detailed error description", example = "Cart with ID 123 is empty")
    private String details;

    @Schema(description = "Timestamp when the error occurred", example = "2026-02-11T17:16:11.484Z")
    private LocalDateTime timestamp;

    @Schema(description = "Request path that caused the error", example = "/api/v1/orders")
    private String path;

    public ErrorResponse(int status, String message, String details, String path) {
        this.status = status;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }
}

