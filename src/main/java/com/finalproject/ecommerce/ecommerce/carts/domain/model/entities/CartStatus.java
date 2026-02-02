package com.finalproject.ecommerce.ecommerce.carts.domain.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_status")
public class CartStatus {

    @Id
    @Column(length = 20)
    private String name;

    @Column(length = 100)
    private String description;
}
