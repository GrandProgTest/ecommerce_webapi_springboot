package com.finalproject.ecommerce.ecommerce.products.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Entity
@IdClass(ProductLike.ProductLikeId.class)
public class ProductLike {

    @Id
    @Column(nullable = false)
    private Long userId;

    @Id
    @Column(nullable = false)
    private Long productId;

    public ProductLike() {
    }

    public ProductLike(Long userId, Long productId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }
        this.userId = userId;
        this.productId = productId;
    }

    public static class ProductLikeId implements Serializable {
        private Long userId;
        private Long productId;

        public ProductLikeId() {
        }

        public ProductLikeId(Long userId, Long productId) {
            this.userId = userId;
            this.productId = productId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductLikeId that = (ProductLikeId) o;
            return userId.equals(that.userId) && productId.equals(that.productId);
        }

        @Override
        public int hashCode() {
            return 31 * userId.hashCode() + productId.hashCode();
        }
    }
}
