package com.finalproject.ecommerce.ecommerce.products.domain.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
public class ProductLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    @Column(nullable = false)
    private Boolean isActive;

    public ProductLike() {
    }

    public ProductLike(Long userId, Product product) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        this.userId = userId;
        this.product = product;
        this.isActive = true;
    }

    public Long getProductId() {
        return product != null ? product.getId() : null;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
