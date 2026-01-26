package com.finalproject.ecommerce.ecommerce.products.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_like")
@IdClass(ProductLike.ProductLikeId.class)
@Getter
public class ProductLike {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "liked_at", nullable = false, updatable = false)
    private LocalDateTime likedAt;

    protected ProductLike() {
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
        this.likedAt = LocalDateTime.now();
    }

    public static class ProductLikeId implements Serializable {
        private Long userId;
        private Long productId;

        public ProductLikeId() {}

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
