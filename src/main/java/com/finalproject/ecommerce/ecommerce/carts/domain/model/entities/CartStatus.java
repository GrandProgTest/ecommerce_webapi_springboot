package com.finalproject.ecommerce.ecommerce.carts.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.valueobjects.CartStatuses;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class CartStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true, length = 20)
    private String name;

    @Column(length = 100)
    private String description;

    public CartStatus(CartStatuses status) {
        this.name = status.name();
    }

    public CartStatus(CartStatuses status, String description) {
        this.name = status.name();
        this.description = description;
    }

    public String getStringName() {
        return name;
    }

    public String getName() {
        return name;
    }

    public static CartStatus fromEnum(CartStatuses status) {
        return new CartStatus(status);
    }

    public CartStatuses toEnum() {
        return CartStatuses.valueOf(this.name);
    }

    public boolean isActive() {
        return CartStatuses.ACTIVE.name().equals(this.name);
    }

    public boolean isCheckedOut() {
        return CartStatuses.CHECKED_OUT.name().equals(this.name);
    }

    public boolean isAbandoned() {
        return CartStatuses.ABANDONED.name().equals(this.name);
    }
}
