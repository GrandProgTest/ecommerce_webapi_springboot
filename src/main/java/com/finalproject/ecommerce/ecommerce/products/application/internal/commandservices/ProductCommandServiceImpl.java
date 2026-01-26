package com.finalproject.ecommerce.ecommerce.products.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.AssignCategoryToProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UpdateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductCommandService;
import com.finalproject.ecommerce.ecommerce.products.repositories.CategoryRepository;
import com.finalproject.ecommerce.ecommerce.products.repositories.ProductCategoryRepository;
import com.finalproject.ecommerce.ecommerce.products.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of the ProductCommandService interface.
 * <p>This class is responsible for handling the commands related to the Product aggregate. It requires a ProductRepository, CategoryRepository, and ProductCategoryRepository.</p>
 * @see ProductCommandService
 * @see ProductRepository
 * @see CategoryRepository
 * @see ProductCategoryRepository
 */
@Service
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;

    /**
     * Constructor of the class.
     * @param productRepository the product repository to be used by the class.
     * @param categoryRepository the category repository to be used by the class.
     * @param productCategoryRepository the product category repository to be used by the class.
     */
    public ProductCommandServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ProductCategoryRepository productCategoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    // inherit javadoc
    @Override
    public Long handle(CreateProductCommand command) {
        var product = new Product(command);
        try {
            productRepository.save(product);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error saving product: %s".formatted(e.getMessage()));
        }
        return product.getId();
    }

    // inherit javadoc
    @Override
    public Optional<Product> handle(UpdateProductCommand command) {
        var result = productRepository.findById(command.productId());
        if (result.isEmpty())
            throw new IllegalArgumentException("Product with id %s not found".formatted(command.productId()));
        var productToUpdate = result.get();
        try {
            productToUpdate.updateProductInfo(
                command.name(),
                command.description(),
                command.price(),
                command.stock()
            );
            var updatedProduct = productRepository.save(productToUpdate);
            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while updating product: %s".formatted(e.getMessage()));
        }
    }

    // inherit javadoc
    @Override
    public void handle(DeleteProductCommand command) {
        if (!productRepository.existsById(command.productId())) {
            throw new IllegalArgumentException("Product with id %s not found".formatted(command.productId()));
        }
        try {
            productRepository.deleteById(command.productId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while deleting product: %s".formatted(e.getMessage()));
        }
    }

    // inherit javadoc
    @Override
    public Optional<Product> handle(AssignCategoryToProductCommand command) {
        // Validate that product exists
        if (!productRepository.existsById(command.productId())) {
            throw new IllegalArgumentException("Product with id %s not found".formatted(command.productId()));
        }

        // Validate that category exists
        if (!categoryRepository.existsById(command.categoryId())) {
            throw new IllegalArgumentException("Category with id %s not found".formatted(command.categoryId()));
        }

        // Check for duplicate assignment
        if (productCategoryRepository.existsByProductIdAndCategoryId(command.productId(), command.categoryId())) {
            throw new IllegalArgumentException("Category with id %s is already assigned to product with id %s".formatted(command.categoryId(), command.productId()));
        }

        try {
            return productRepository.findById(command.productId()).map(product -> {
                product.assignCategory(command.categoryId());
                productRepository.save(product);
                return product;
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while assigning category to product: %s".formatted(e.getMessage()));
        }
    }
}
