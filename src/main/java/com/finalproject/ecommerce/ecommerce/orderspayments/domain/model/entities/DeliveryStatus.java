package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.DeliveryStatuses;
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
public class DeliveryStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    public DeliveryStatus(DeliveryStatuses status) {
        this.name = status.name();
    }

    public DeliveryStatus(DeliveryStatuses status, String description) {
        this.name = status.name();
        this.description = description;
    }

    public static DeliveryStatus fromEnum(DeliveryStatuses status) {
        return new DeliveryStatus(status);
    }

    public DeliveryStatuses toEnum() {
        return DeliveryStatuses.valueOf(this.name);
    }

    public boolean isDelivered() {
        return DeliveryStatuses.DELIVERED.name().equals(this.name);
    }
}

