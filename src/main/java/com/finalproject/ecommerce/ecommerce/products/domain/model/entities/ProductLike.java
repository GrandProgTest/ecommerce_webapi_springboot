package com.finalproject.ecommerce.ecommerce.products.domain.model.entities;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

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
    }

    public Long getProductId() {
        return product != null ? product.getId() : null;
    }
}
