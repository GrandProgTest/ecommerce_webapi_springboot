package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;

import java.time.LocalDateTime;

public record CategoryResource(Long id, String name, LocalDateTime createdAt) {
}
