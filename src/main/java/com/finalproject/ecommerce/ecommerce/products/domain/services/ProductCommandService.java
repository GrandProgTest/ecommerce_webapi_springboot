package com.finalproject.ecommerce.ecommerce.products.domain.services;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.*;

import java.util.Optional;

public interface ProductCommandService {
    Long handle(CreateProductCommand command);

    Optional<Product> handle(UpdateProductCommand command);

    void handle(DeleteProductCommand command);

    Optional<Product> handle(AssignCategoryToProductCommand command);

    boolean handle(ToggleProductLikeCommand command);

    Optional<Product> handle(DecreaseProductStockCommand command);

    Optional<Product> handle(IncreaseProductStockCommand command);

    Optional<Product> handle(ActivateProductCommand command);

    Optional<Product> handle(DeactivateProductCommand command);

    Optional<Product> handle(SoftDeleteProductCommand command);
}
