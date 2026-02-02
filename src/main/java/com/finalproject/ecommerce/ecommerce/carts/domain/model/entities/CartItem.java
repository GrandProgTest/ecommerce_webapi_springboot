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
@Table(name = "cart_item", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cart_product", columnNames = {"cart_id", "product_id"})
})
public class CartItem extends AuditableModel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_cart_item_cart"))
    private Cart cart;

    @NotNull
    @Column(name = "product_id", nullable = false)
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
