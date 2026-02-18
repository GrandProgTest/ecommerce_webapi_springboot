package com.finalproject.ecommerce.ecommerce.products.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.valueobjects.ImageUrl;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
public class ProductImage extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @Column(name = "product_id", insertable = false, updatable = false)
    private Long productId;

    @NotNull
    @Embedded
    @AttributeOverride(name = "url", column = @Column(nullable = false, columnDefinition = "TEXT"))
    private ImageUrl imageUrl;

    @NotNull
    @Column(nullable = false)
    private Boolean isPrimary;


    public ProductImage() {
    }

    public ProductImage(Product product, ImageUrl imageUrl, Boolean isPrimary) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("Image URL cannot be null");
        }
        this.product = product;
        this.imageUrl = imageUrl;
        this.isPrimary = isPrimary != null ? isPrimary : false;
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
