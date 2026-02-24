package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.mapper;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.Category;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class ProductGraphQLMapper {


    public record ProductGraphQLResource(Long id, String name, String description, Double price,
                                         Double salePrice, Instant salePriceExpireDate,
                                         Double effectivePrice, Boolean hasActiveSalePrice,
                                         Integer stock, Boolean isActive, List<Long> categoryIds,
                                         Long createdByUserId, Integer likeCount,
                                         Boolean isLikedByCurrentUser, Boolean isInStock,
                                         String primaryImageUrl, Instant createdAt, Instant updatedAt) {
    }

    public record ProductPageGraphQLResource(List<ProductGraphQLResource> products,
                                             PageMetadataGraphQLResource pageMetadata) {
    }

    public record PageMetadataGraphQLResource(int currentPage, int pageSize, long totalElements,
                                              int totalPages, boolean hasNext, boolean hasPrevious) {
    }

    public record CategoryGraphQLResource(Long id, String name, Instant createdAt) {
    }

    public record DeleteProductGraphQLResponse(Boolean success, String message) {
    }

    public record LikeProductGraphQLResponse(Boolean success, Boolean isLiked, Integer likeCount, String message) {
    }

    public record CreateProductGraphQLInput(String name, String description, Double price, Integer stock,
                                            List<Long> categoryIds) {
    }

    public record CreateCategoryGraphQLInput(String name) {
    }

    public record SetProductSalePriceGraphQLInput(Double salePrice, Instant salePriceExpireDate) {
    }

    public static ProductGraphQLResource toResource(Product product) {
        return new ProductGraphQLResource(
                product.getId(), product.getName(), product.getDescription(),
                product.getPrice().doubleValue(),
                product.getSalePrice() != null ? product.getSalePrice().doubleValue() : null,
                product.getSalePriceExpireDate(),
                product.getEffectivePrice().doubleValue(),
                product.hasActiveSalePrice(),
                product.getStock(), product.getIsActive(),
                product.getCategoryIds(), product.getCreatedByUserId(),
                product.getLikesCount(), null,
                product.getStock() > 0, product.getPrimaryImageUrl(),
                product.getCreatedAt(), product.getUpdatedAt()
        );
    }

    public static ProductGraphQLResource toResource(Product product, Long userId) {
        boolean isLiked = product.isLikedByUser(userId);
        return new ProductGraphQLResource(
                product.getId(), product.getName(), product.getDescription(),
                product.getPrice().doubleValue(),
                product.getSalePrice() != null ? product.getSalePrice().doubleValue() : null,
                product.getSalePriceExpireDate(),
                product.getEffectivePrice().doubleValue(),
                product.hasActiveSalePrice(),
                product.getStock(), product.getIsActive(),
                product.getCategoryIds(), product.getCreatedByUserId(),
                product.getLikesCount(), isLiked,
                product.getStock() > 0, product.getPrimaryImageUrl(),
                product.getCreatedAt(), product.getUpdatedAt()
        );
    }

    public static CategoryGraphQLResource toResource(Category category) {
        return new CategoryGraphQLResource(category.getId(), category.getName(), category.getCreatedAt());
    }

    public static LikeProductGraphQLResponse toLikeResponse(Product product, boolean isLiked) {
        String message = isLiked ? "Product liked successfully" : "Product unliked successfully";
        return new LikeProductGraphQLResponse(true, isLiked, product.getLikesCount(), message);
    }

    public static CreateProductCommand toCreateProductCommand(CreateProductGraphQLInput input) {
        return new CreateProductCommand(
                input.name(), input.description(),
                BigDecimal.valueOf(input.price()), input.stock(),
                input.categoryIds(), true
        );
    }

    public static CreateCategoryCommand toCreateCategoryCommand(CreateCategoryGraphQLInput input) {
        return new CreateCategoryCommand(input.name());
    }

    public static DeleteProductCommand toDeleteCommand(Long id) {
        return new DeleteProductCommand(id);
    }

    public static ActivateProductCommand toActivateCommand(Long id) {
        return new ActivateProductCommand(id);
    }

    public static DeactivateProductCommand toDeactivateCommand(Long id) {
        return new DeactivateProductCommand(id);
    }

    public static ToggleProductLikeCommand toLikeCommand(Long userId, Long productId) {
        return new ToggleProductLikeCommand(userId, productId);
    }

    public static SetProductSalePriceCommand toSetSalePriceCommand(Long productId, SetProductSalePriceGraphQLInput input) {
        return new SetProductSalePriceCommand(
                productId,
                BigDecimal.valueOf(input.salePrice()),
                input.salePriceExpireDate()
        );
    }
}

