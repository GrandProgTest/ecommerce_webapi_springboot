package com.finalproject.ecommerce.ecommerce.products.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.products.domain.model.valueobjects.ImageUrl;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.util.Date;

@Getter
@Entity
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Long productId;

    @NotNull
    @Embedded
    @AttributeOverride(name = "url", column = @Column(nullable = false, columnDefinition = "TEXT"))
    private ImageUrl imageUrl;

    @NotNull
    @Column(nullable = false)
    private Boolean isPrimary;

    @Column(nullable = false, updatable = false)
    private Date createdAt;

    public ProductImage() {
    }

    public ProductImage(Long productId, ImageUrl imageUrl, Boolean isPrimary) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("Image URL cannot be null");
        }
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.isPrimary = isPrimary != null ? isPrimary : false;
        this.createdAt = new Date();
    }

    public void setAsPrimary() {
        this.isPrimary = true;
    }

    public void unsetAsPrimary() {
        this.isPrimary = false;
    }

    public String getUrl() {
        return imageUrl.url();
    }
}
