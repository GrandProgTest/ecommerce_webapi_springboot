package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;


import java.time.Instant;

public record CategoryResource(Long id, String name, Instant createdAt) {
}
