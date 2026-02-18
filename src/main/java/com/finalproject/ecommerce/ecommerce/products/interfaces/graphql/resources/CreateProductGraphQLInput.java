package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources;

import java.util.List;

public record CreateProductGraphQLInput(String name, String description, Double price, Integer stock,
                                        List<Long> categoryIds) {
}

