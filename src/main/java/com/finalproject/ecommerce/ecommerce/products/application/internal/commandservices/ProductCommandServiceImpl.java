package com.finalproject.ecommerce.ecommerce.products.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.acl.OrdersContextFacade;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.CategoryNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.DuplicateCategoryAssignmentException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductInOrdersException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.ActivateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.AssignCategoryToProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeactivateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DecreaseProductStockCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.IncreaseProductStockCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.ToggleProductLikeCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UpdateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductCommandService;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductCategoryRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final IamContextFacade iamContextFacade;
    private final OrdersContextFacade ordersContextFacade;

    public ProductCommandServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ProductCategoryRepository productCategoryRepository, IamContextFacade iamContextFacade, OrdersContextFacade ordersContextFacade) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.iamContextFacade = iamContextFacade;
        this.ordersContextFacade = ordersContextFacade;
    }

    @Override
    public Long handle(CreateProductCommand command) {
        var userId = iamContextFacade.getCurrentUserId().orElseThrow(() -> new IllegalStateException("User not authenticated"));

        if (command.categoryIds() == null || command.categoryIds().isEmpty()) {
            throw new IllegalArgumentException("At least one category is required");
        }

        var product = new Product(command, userId);

        for (Long categoryId : command.categoryIds()) {
            var category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId));
            product.assignCategory(category);
        }

        try {
            productRepository.save(product);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error saving product: %s".formatted(e.getMessage()));
        }
        return product.getId();
    }

    @Override
    public Optional<Product> handle(UpdateProductCommand command) {
        var result = productRepository.findById(command.productId());
        if (result.isEmpty()) throw new ProductNotFoundException(command.productId());
        var productToUpdate = result.get();
        try {
            productToUpdate.updateProductInfo(command.name(), command.description(), command.price(), command.stock());
            var updatedProduct = productRepository.save(productToUpdate);
            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while updating product: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public void handle(DeleteProductCommand command) {
        if (!productRepository.existsById(command.productId())) {
            throw new ProductNotFoundException(command.productId());
        }

        if (ordersContextFacade.productExistsInOrders(command.productId())) {
            throw new ProductInOrdersException(command.productId());
        }

        try {
            productRepository.deleteById(command.productId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while deleting product: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public Optional<Product> handle(AssignCategoryToProductCommand command) {
        if (!productRepository.existsById(command.productId())) {
            throw new ProductNotFoundException(command.productId());
        }

        var category = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(command.categoryId()));

        if (productCategoryRepository.existsByProduct_IdAndCategory_Id(command.productId(), command.categoryId())) {
            throw new DuplicateCategoryAssignmentException(command.productId(), command.categoryId());
        }

        try {
            return productRepository.findById(command.productId()).map(product -> {
                product.assignCategory(category);
                productRepository.save(product);
                return product;
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while assigning category to product: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public boolean handle(ToggleProductLikeCommand command) {
        final Long userId;
        if (command.userId() == null) {
            userId = iamContextFacade.getCurrentUserId()
                    .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        } else {
            iamContextFacade.validateUserCanAccessResource(command.userId());
            userId = command.userId();
        }

        var product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        try {
            boolean isLiked = product.toggleLike(userId);
            productRepository.save(product);
            return isLiked;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while toggling product like: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public Optional<Product> handle(DecreaseProductStockCommand command) {
        var product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        try {
            product.decreaseStock(command.quantity());
            var updatedProduct = productRepository.save(product);
            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while decreasing product stock: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public Optional<Product> handle(IncreaseProductStockCommand command) {
        var product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        try {
            product.increaseStock(command.quantity());
            var updatedProduct = productRepository.save(product);
            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while increasing product stock: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public Optional<Product> handle(ActivateProductCommand command) {
        var product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        try {
            product.activate();
            var updatedProduct = productRepository.save(product);
            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while activating product: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public Optional<Product> handle(DeactivateProductCommand command) {
        var product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        try {
            product.deactivate();
            var updatedProduct = productRepository.save(product);
            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while deactivating product: %s".formatted(e.getMessage()));
        }
    }
}
