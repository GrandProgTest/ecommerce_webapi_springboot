package com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.carts.domain.exceptions.InvalidCartOperationException;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartItem;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartStatus;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Cart extends AuditableAbstractAggregateRoot<Cart> {

    @NotNull
    @Column(nullable = false, unique = true)
    private Long userId;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(nullable = false)
    private CartStatus status;

    @Column
    private Date checkedOutAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> items = new ArrayList<>();

    public Cart(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        this.userId = userId;
        this.items = new ArrayList<>();
    }

    public Cart(Long userId, CartStatus status) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        this.userId = userId;
        this.status = status;
        this.items = new ArrayList<>();
    }


    public void addProduct(Long productId, Integer quantity) {
        validateActiveStatus();

        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Optional<CartItem> existingItem = findItemByProductId(productId);

        if (existingItem.isPresent()) {
            existingItem.get().increaseQuantity(quantity);
        } else {
            CartItem newItem = new CartItem(this, productId, quantity);
            this.items.add(newItem);
        }
    }

    public void updateProductQuantity(Long productId, Integer newQuantity) {
        validateActiveStatus();

        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        CartItem item = findItemByProductId(productId).orElseThrow(() -> new InvalidCartOperationException("Product not found in cart"));

        item.updateQuantity(newQuantity);
    }

    public void updateCartItemQuantity(Long cartItemId, Integer newQuantity) {
        validateActiveStatus();

        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        CartItem item = findItemById(cartItemId).orElseThrow(() -> new InvalidCartOperationException("Cart item not found"));

        item.updateQuantity(newQuantity);
    }


    public void removeProduct(Long productId) {
        validateActiveStatus();

        CartItem item = findItemByProductId(productId).orElseThrow(() -> new InvalidCartOperationException("Product not found in cart"));

        this.items.remove(item);
    }

    public void removeCartItem(Long cartItemId) {
        validateActiveStatus();

        CartItem item = findItemById(cartItemId).orElseThrow(() -> new InvalidCartOperationException("Cart item not found"));

        this.items.remove(item);
    }

    public void clear() {
        validateActiveStatus();
        this.items.clear();
    }


    public void checkout(CartStatus checkedOutStatus) {
        validateActiveStatus();

        if (this.items.isEmpty()) {
            throw new InvalidCartOperationException("Cannot checkout an empty cart");
        }

        this.status = checkedOutStatus;
        this.checkedOutAt = new Date();
    }

    public void markAsAbandoned(CartStatus abandonedStatus) {
        validateActiveStatus();
        this.status = abandonedStatus;
    }


    private Optional<CartItem> findItemByProductId(Long productId) {
        return this.items.stream().filter(item -> item.isForProduct(productId)).findFirst();
    }

    private Optional<CartItem> findItemById(Long cartItemId) {
        return this.items.stream().filter(item -> item.getId().equals(cartItemId)).findFirst();
    }

    public boolean containsProduct(Long productId) {
        return findItemByProductId(productId).isPresent();
    }

    public Integer getProductQuantity(Long productId) {
        return findItemByProductId(productId).map(CartItem::getQuantity).orElse(0);
    }

    public int getTotalItems() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public boolean isActive() {
        return this.status.isActive();
    }

    private void validateActiveStatus() {
        if (!isActive()) {
            throw new InvalidCartOperationException("Cannot modify cart with status: " + this.status.getStringName());
        }
    }
}
