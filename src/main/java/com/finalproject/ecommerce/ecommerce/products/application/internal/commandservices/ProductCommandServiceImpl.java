package com.finalproject.ecommerce.ecommerce.products.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.CategoryNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.DuplicateCategoryAssignmentException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.AssignCategoryToProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductCommand;
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

    public ProductCommandServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ProductCategoryRepository productCategoryRepository, IamContextFacade iamContextFacade) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    public Long handle(CreateProductCommand command) {
        var userId = iamContextFacade.getCurrentUserId().orElseThrow(() -> new IllegalStateException("User not authenticated"));

        var product = new Product(command, userId);

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

        if (productCategoryRepository.existsByProductIdAndCategoryId(command.productId(), command.categoryId())) {
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
        var userId = iamContextFacade.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

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

    public Optional<Product> decreaseProductStock(Long productId, Integer quantity) {
        var result = productRepository.findById(productId);
        if (result.isEmpty()) throw new ProductNotFoundException(productId);
        var product = result.get();
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product with id %d".formatted(productId));
        }
        try {
            product.setStock(product.getStock() - quantity);
            var updatedProduct = productRepository.save(product);
            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while decreasing product stock: %s".formatted(e.getMessage()));
        }
    }
}
