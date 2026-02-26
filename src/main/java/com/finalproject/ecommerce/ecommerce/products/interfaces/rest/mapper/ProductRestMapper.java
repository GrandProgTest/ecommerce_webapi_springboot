package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.mapper;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.Category;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductImage;
import com.finalproject.ecommerce.ecommerce.shared.interfaces.rest.resources.PageMetadata;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class ProductRestMapper {

    public record ProductResource(Long id, String name, String description, BigDecimal price,
                                  BigDecimal salePrice, Instant salePriceExpireDate,
                                  BigDecimal effectivePrice, Boolean hasActiveSalePrice,
                                  Integer stock, Boolean isActive, Boolean isDeleted,
                                  List<Long> categoryIds, Long createdByUserId, String primaryImageUrl) {
    }

    public record ProductDetailResource(Long id, String name, String description, BigDecimal price,
                                        BigDecimal salePrice, Instant salePriceExpireDate,
                                        BigDecimal effectivePrice, Boolean hasActiveSalePrice,
                                        Integer stock, Boolean isActive, Boolean isDeleted,
                                        List<Long> categoryIds, Long createdByUserId,
                                        List<ProductImageResource> images) {
    }

    public record ProductImageResource(Long id, Long productId, String imageUrl, Boolean isPrimary, Instant createdAt) {
    }

    public record PaginatedProductResponse(List<ProductResource> products, PageMetadata pageMetadata) {
    }

    public record CategoryResource(Long id, String name, Instant createdAt) {
    }

    public record ToggleProductLikeResource(Long productId, boolean isLiked, int totalLikes, String message) {
    }

    public record UploadMultipleImagesResponse(String status, String message, int uploadedCount, int totalFiles,
                                               List<ImageUploadResult> images) {
        public record ImageUploadResult(Long imageId, String imageUrl, Boolean isPrimary, boolean success,
                                        String errorMessage) {
            public static ImageUploadResult success(Long imageId, String imageUrl, Boolean isPrimary) {
                return new ImageUploadResult(imageId, imageUrl, isPrimary, true, null);
            }

            public static ImageUploadResult failure(String errorMessage) {
                return new ImageUploadResult(null, null, false, false, errorMessage);
            }
        }

        public static UploadMultipleImagesResponse success(List<ImageUploadResult> images) {
            long c = images.stream().filter(ImageUploadResult::success).count();
            return new UploadMultipleImagesResponse("SUCCESS", "Uploaded " + c + " of " + images.size() + " images", (int) c, images.size(), images);
        }

        public static UploadMultipleImagesResponse partial(List<ImageUploadResult> images) {
            long c = images.stream().filter(ImageUploadResult::success).count();
            return new UploadMultipleImagesResponse("PARTIAL_SUCCESS", "Uploaded " + c + " of " + images.size() + " images successfully", (int) c, images.size(), images);
        }

        public static UploadMultipleImagesResponse failure(String message, int totalFiles) {
            return new UploadMultipleImagesResponse("FAILURE", message, 0, totalFiles, List.of());
        }
    }

    public record CreateProductResource(String name, String description, BigDecimal price, Integer stock,
                                        List<Long> categoryIds, Boolean isActive) {
    }

    public record UpdateProductResource(String name, String description, BigDecimal price, Integer stock) {
    }

    public record CreateCategoryResource(String name) {
    }

    public record UpdateCategoryResource(String name) {
    }

    public record SetProductSalePriceResource(BigDecimal salePrice, Instant salePriceExpireDate) {
    }

    public static ProductResource toResource(Product entity) {
        return new ProductResource(entity.getId(), entity.getName(), entity.getDescription(), entity.getPrice(), entity.getSalePrice(), entity.getSalePriceExpireDate(), entity.getEffectivePrice(), entity.hasActiveSalePrice(), entity.getStock(), entity.getIsActive(), entity.getIsDeleted(), entity.getCategoryIds(), entity.getCreatedByUserId(), entity.getPrimaryImageUrl());
    }

    public static ProductDetailResource toDetailResource(Product entity) {
        var images = entity.getImages().stream().map(ProductRestMapper::toImageResource).toList();
        return new ProductDetailResource(entity.getId(), entity.getName(), entity.getDescription(), entity.getPrice(), entity.getSalePrice(), entity.getSalePriceExpireDate(), entity.getEffectivePrice(), entity.hasActiveSalePrice(), entity.getStock(), entity.getIsActive(), entity.getIsDeleted(), entity.getCategoryIds(), entity.getCreatedByUserId(), images);
    }

    public static ProductImageResource toImageResource(ProductImage entity) {
        return new ProductImageResource(entity.getId(), entity.getProductId(), entity.getUrl(), entity.getIsPrimary(), entity.getCreatedAt());
    }

    public static CategoryResource toResource(Category entity) {
        return new CategoryResource(entity.getId(), entity.getName(), entity.getCreatedAt());
    }

    public static UploadMultipleImagesResponse toUploadResponse(List<ProductImage> images, int totalFiles) {
        if (images == null || images.isEmpty())
            return UploadMultipleImagesResponse.failure("No images were uploaded successfully", totalFiles);
        List<UploadMultipleImagesResponse.ImageUploadResult> results = images.stream().map(i -> UploadMultipleImagesResponse.ImageUploadResult.success(i.getId(), i.getUrl(), i.getIsPrimary())).collect(Collectors.toList());
        return images.size() == totalFiles ? UploadMultipleImagesResponse.success(results) : UploadMultipleImagesResponse.partial(results);
    }

    public static CreateProductCommand toCreateCommand(CreateProductResource r) {
        return new CreateProductCommand(r.name(), r.description(), r.price(), r.stock(), r.categoryIds(), r.isActive());
    }

    public static UpdateProductCommand toUpdateCommand(Long id, UpdateProductResource r) {
        return new UpdateProductCommand(id, r.name(), r.description(), r.price(), r.stock());
    }

    public static SoftDeleteProductCommand toSoftDeleteCommand(Long id) {
        return new SoftDeleteProductCommand(id);
    }

    public static ToggleProductLikeCommand toLikeCommand(Long userId, Long productId) {
        return new ToggleProductLikeCommand(userId, productId);
    }

    public static SetProductSalePriceCommand toSetSalePriceCommand(Long productId, SetProductSalePriceResource r) {
        return new SetProductSalePriceCommand(productId, r.salePrice(), r.salePriceExpireDate());
    }
}

