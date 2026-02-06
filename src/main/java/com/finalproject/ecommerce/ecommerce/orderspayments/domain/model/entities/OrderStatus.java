package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.OrderStatuses;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    public OrderStatus(OrderStatuses status) {
        this.name = status.name();
    }

    public OrderStatus(OrderStatuses status, String description) {
        this.name = status.name();
        this.description = description;
    }

    public String getStringName() {
        return name;
    }

    public String getName() {
        return name;
    }

    public static OrderStatus getDefaultStatus() {
        OrderStatus status = new OrderStatus();
        status.setName(OrderStatuses.PENDING.name());
        return status;
    }

    public static OrderStatus fromEnum(OrderStatuses status) {
        return new OrderStatus(status);
    }

    public OrderStatuses toEnum() {
        return OrderStatuses.valueOf(this.name);
    }

    public boolean isPending() {
        return OrderStatuses.PENDING.name().equals(this.name);
    }

    public boolean isPaid() {
        return OrderStatuses.PAID.name().equals(this.name);
    }

    public boolean isCancelled() {
        return OrderStatuses.CANCELLED.name().equals(this.name);
    }
}
