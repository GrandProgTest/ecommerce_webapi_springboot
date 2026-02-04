package com.finalproject.ecommerce.ecommerce.products.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "product_categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "category_id"}))
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, insertable = false, updatable = false)
    private Category category;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

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
        this.assignedAt = LocalDateTime.now();
    }
}
