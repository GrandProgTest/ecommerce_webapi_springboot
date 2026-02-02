package com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductCategory;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductImage;
import com.finalproject.ecommerce.ecommerce.products.domain.model.valueobjects.ImageUrl;
import com.finalproject.ecommerce.ecommerce.products.domain.model.valueobjects.Money;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
    @Valid
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false, precision = 10, scale = 2))
    private Money price;

    @NotNull
    @Min(value = 0)
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private final List<ProductCategory> productCategories = new ArrayList<>();

    @NotNull
    @Positive
    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private final List<ProductImage> images = new ArrayList<>();

    public Product() {
    }

    public Product(CreateProductCommand command, Long createdByUserId) {
        this.name = command.name();
        this.description = command.description();
        this.price = new Money(command.price());
        this.createdByUserId = createdByUserId;
        this.stock = command.stock();
        this.isActive = true;
    }

    public void updateProductInfo(String name, String description, BigDecimal price, Integer stock) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null) {
            this.price = new Money(price);
        }
        if (stock != null && stock >= 0) {
            this.stock = stock;
        }
    }

    public void assignCategory(Long categoryId) {
        var productCategory = new ProductCategory(this.getId(), categoryId);
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

    public void addImage(ImageUrl imageUrl, Boolean isPrimary) {
        if (isPrimary != null && isPrimary) {
            images.forEach(ProductImage::unsetAsPrimary);
        }
        var image = new ProductImage(this.getId(), imageUrl, isPrimary);
        this.images.add(image);
    }

    public void setPrimaryImage(Long imageId) {
        images.forEach(ProductImage::unsetAsPrimary);
        images.stream()
            .filter(img -> img.getId().equals(imageId))
            .findFirst()
            .ifPresent(ProductImage::setAsPrimary);
    }

    public boolean isActive() {
        return this.isActive;
    }

    public BigDecimal getPriceAmount() {
        return this.price.amount();
    }

    public List<Long> getCategoryIds() {
        return productCategories.stream()
            .map(ProductCategory::getCategoryId)
            .toList();
    }
}
