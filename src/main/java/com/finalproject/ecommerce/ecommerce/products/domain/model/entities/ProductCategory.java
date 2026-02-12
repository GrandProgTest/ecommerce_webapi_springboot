package com.finalproject.ecommerce.ecommerce.products.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;

@Getter
@Entity
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    private Category category;

    @Column(nullable = false, updatable = false)
    private Date assignedAt;

    public ProductCategory() {
    }

    public ProductCategory(Product product, Category category) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        this.product = product;
        this.category = category;
        this.assignedAt = new Date();
    }

    public Long getProductId() {
        return product != null ? product.getId() : null;
    }

    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }
}
