package com.finalproject.ecommerce.ecommerce.carts.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class CartItem extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Cart cart;

    @NotNull
    @Column(nullable = false)
    private Long productId;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    public CartItem(Cart cart, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        this.cart = cart;
        this.productId = productId;
        this.quantity = quantity;
    }

    public void increaseQuantity(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        this.quantity += amount;
    }

    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        this.quantity = newQuantity;
    }

    public void decreaseQuantity(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (this.quantity - amount < 0) {
            throw new IllegalArgumentException("Cannot decrease quantity below 0");
        }
        this.quantity -= amount;
    }

    public boolean isForProduct(Long productId) {
        return this.productId.equals(productId);
    }
}
