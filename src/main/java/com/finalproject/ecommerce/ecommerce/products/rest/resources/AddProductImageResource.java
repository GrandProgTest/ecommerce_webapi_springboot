package com.finalproject.ecommerce.ecommerce.products.rest.resources;

public record AddProductImageResource(
    String imageUrl,
    Boolean isPrimary
) {}
