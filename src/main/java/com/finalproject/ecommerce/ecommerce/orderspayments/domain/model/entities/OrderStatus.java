package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "order_status")
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    protected OrderStatus() {
    }

    public OrderStatus(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
