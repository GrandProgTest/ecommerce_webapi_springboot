package com.finalproject.ecommerce.ecommerce.products.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;

@Getter
@Entity
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date assignedAt;

    public ProductCategory() {
    }

    public ProductCategory(Long productId, Long categoryId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        this.productId = productId;
        this.categoryId = categoryId;
        this.assignedAt = new Date();
    }
}
