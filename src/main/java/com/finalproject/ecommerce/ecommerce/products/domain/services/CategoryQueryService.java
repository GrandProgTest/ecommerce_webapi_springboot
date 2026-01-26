package com.finalproject.ecommerce.ecommerce.products.domain.services;

import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.Category;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetAllCategoriesQuery;

import java.util.List;
import java.util.Optional;

public interface CategoryQueryService {
    List<Category> handle (GetAllCategoriesQuery query);
    Optional<Category> findById(Long categoryId);
}
