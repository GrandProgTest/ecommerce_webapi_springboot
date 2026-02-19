package com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductAlreadyLikedException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductNotLikedException;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.annotations.ValidPrice;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.Category;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductCategory;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductImage;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductLike;
import com.finalproject.ecommerce.ecommerce.products.domain.model.valueobjects.ImageUrl;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Entity
public class Product extends AuditableAbstractAggregateRoot<Product> {

    @NotBlank
    @Size(min = 2, max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @ValidPrice
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ValidPrice
    @Column(precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Column
    private Instant salePriceExpireDate;

    @NotNull
    @Min(value = 0)
    @Column(nullable = false)
    private Integer stock;

    @NotNull
    @Column(nullable = false)
    private Boolean isActive;

    @NotNull
    @Column(nullable = false)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<ProductCategory> productCategories = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<ProductLike> likes = new ArrayList<>();

    @NotNull
    @Positive
    @Column(nullable = false)
    private Long createdByUserId;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<ProductImage> images = new ArrayList<>();

    public Product() {
    }

    public Product(CreateProductCommand command, Long createdByUserId) {
        this.name = command.name();
        this.description = command.description();
        this.price = command.price();
        this.createdByUserId = createdByUserId;
        this.stock = command.stock();
        this.isActive = command.isActive();
    }

    public void updateProductInfo(String name, String description, BigDecimal price, Integer stock) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null && price.compareTo(BigDecimal.ONE) >= 0) {
            this.price = price;
        }
        if (stock != null && stock >= 0) {
            this.stock = stock;
        }
    }

    public void assignCategory(Category category) {
        var productCategory = new ProductCategory(this, category);
        this.productCategories.add(productCategory);
    }

    public void removeCategory(Long categoryId) {
        this.productCategories.removeIf(pc -> pc.getCategoryId().equals(categoryId));
    }

    public void activate() {
        if (stock <= 0) {
            throw new IllegalStateException("Cannot activate product with no stock");
        }
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.isActive = false;
    }

    public void restore() {
        this.isDeleted = false;
    }

    public void addImage(ImageUrl imageUrl, Boolean isPrimary) {
        if (isPrimary != null && isPrimary) {
            images.forEach(ProductImage::unsetAsPrimary);
        }
        var image = new ProductImage(this, imageUrl, isPrimary);
        this.images.add(image);
    }

    public boolean isActive() {
        return this.isActive;
    }

    public List<Long> getCategoryIds() {
        return productCategories.stream()
                .map(ProductCategory::getCategoryId)
                .toList();
    }

    public int getLikesCount() {
        return (int) likes.stream()
                .filter(ProductLike::getIsActive)
                .count();
    }

    public List<Long> getLikedByUserIds() {
        return likes.stream()
                .filter(ProductLike::getIsActive)
                .map(ProductLike::getUserId)
                .toList();
    }

    public boolean isLikedByUser(Long userId) {
        return likes.stream()
                .anyMatch(like -> like.getUserId().equals(userId) && like.getIsActive());
    }

    public void addLike(Long userId) {
        if (isLikedByUser(userId)) {
            throw new ProductAlreadyLikedException(userId, this.getId());
        }

        var existingLike = likes.stream()
                .filter(like -> like.getUserId().equals(userId))
                .findFirst();

        if (existingLike.isPresent()) {
            existingLike.get().activate();
        } else {
            var like = new ProductLike(userId, this);
            this.likes.add(like);
        }
    }

    public void removeLike(Long userId) {
        var like = likes.stream()
                .filter(l -> l.getUserId().equals(userId) && l.getIsActive())
                .findFirst()
                .orElseThrow(() -> new ProductNotLikedException(userId, this.getId()));

        like.deactivate();
    }

    public boolean toggleLike(Long userId) {
        if (isLikedByUser(userId)) {
            removeLike(userId);
            return false;
        } else {
            addLike(userId);
            return true;
        }
    }

    public void decreaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (this.stock < quantity) {
            throw new IllegalArgumentException("Insufficient stock. Available: %d, Requested: %d".formatted(this.stock, quantity));
        }
        this.stock -= quantity;
    }

    public void increaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.stock += quantity;
    }

    public String getPrimaryImageUrl() {
        return images.stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .map(ProductImage::getUrl)
                .orElse(null);
    }

    public List<ProductImage> getImages() {
        return new ArrayList<>(images);
    }

    public void setSalePrice(BigDecimal salePrice, java.time.Instant salePriceExpireDate) {
        if (salePrice != null) {
            if (salePrice.compareTo(this.price) >= 0) {
                throw new IllegalArgumentException("Sale price must be less than the base price");
            }
            if (salePriceExpireDate == null) {
                throw new IllegalArgumentException("Sale price expire date is required");
            }
            java.time.Instant minimumExpireDate = java.time.Instant.now().plus(java.time.Duration.ofHours(24));
            if (salePriceExpireDate.isBefore(minimumExpireDate)) {
                throw new IllegalArgumentException("Sale price expire date must be at least 24 hours from now");
            }
        }
        this.salePrice = salePrice;
        this.salePriceExpireDate = salePriceExpireDate;
    }

    public boolean hasActiveSalePrice() {
        if (this.salePrice == null || this.salePriceExpireDate == null) {
            return false;
        }
        return this.salePriceExpireDate.isAfter(java.time.Instant.now());
    }

    public BigDecimal getEffectivePrice() {
        if (hasActiveSalePrice()) {
            return this.salePrice;
        }
        return this.price;
    }
}
