package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.AssignCategoryToProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetAllProductsQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductByIdQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductCommandService;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductQueryService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.CreateProductInput;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.ProductResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.UpdateProductInput;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform.CreateProductCommandFromInputAssembler;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform.ProductResourceFromEntityAssembler;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform.UpdateProductCommandFromInputAssembler;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ProductGraphQLController {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;

    public ProductGraphQLController(ProductCommandService productCommandService, ProductQueryService productQueryService) {
        this.productCommandService = productCommandService;
        this.productQueryService = productQueryService;
    }

    @MutationMapping
    public ProductResource createProduct(@Argument CreateProductInput input) {
        var createProductCommand = CreateProductCommandFromInputAssembler.toCommandFromInput(input);
        var productId = productCommandService.handle(createProductCommand);
        if (productId == null || productId == 0L) {
            throw new RuntimeException("Failed to create product");
        }
        var getProductByIdQuery = new GetProductByIdQuery(productId);
        var product = productQueryService.handle(getProductByIdQuery);
        if (product.isEmpty()) {
            throw new RuntimeException("Product not found after creation");
        }
        return ProductResourceFromEntityAssembler.toResourceFromEntity(product.get());
    }

    @MutationMapping
    public ProductResource updateProduct(@Argument Long id, @Argument UpdateProductInput input) {
        var updateProductCommand = UpdateProductCommandFromInputAssembler.toCommandFromInput(id, input);
        var updatedProduct = productCommandService.handle(updateProductCommand);
        if (updatedProduct.isEmpty()) {
            throw new RuntimeException("Product not found");
        }
        return ProductResourceFromEntityAssembler.toResourceFromEntity(updatedProduct.get());
    }

    @MutationMapping
    public String deleteProduct(@Argument Long id) {
        var deleteProductCommand = new DeleteProductCommand(id);
        productCommandService.handle(deleteProductCommand);
        return "Product with id " + id + " successfully deleted";
    }

    @MutationMapping
    public ProductResource assignCategoryToProduct(@Argument Long productId, @Argument Long categoryId) {
        var assignCategoryCommand = new AssignCategoryToProductCommand(productId, categoryId);
        var updatedProduct = productCommandService.handle(assignCategoryCommand);
        if (updatedProduct.isEmpty()) {
            throw new RuntimeException("Product or category not found");
        }
        return ProductResourceFromEntityAssembler.toResourceFromEntity(updatedProduct.get());
    }

    @QueryMapping
    public ProductResource getProductById(@Argument Long id) {
        var getProductByIdQuery = new GetProductByIdQuery(id);
        var product = productQueryService.handle(getProductByIdQuery);
        if (product.isEmpty()) {
            throw new RuntimeException("Product not found");
        }
        return ProductResourceFromEntityAssembler.toResourceFromEntity(product.get());
    }

    @QueryMapping
    public List<ProductResource> getAllProducts() {
        var products = productQueryService.handle(new GetAllProductsQuery());
        return products.stream()
                .map(ProductResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }
}
