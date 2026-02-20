package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Getter
public class Discount extends AuditableAbstractAggregateRoot<Discount> {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private Integer percentage;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    @Column(nullable = false)
    private Boolean isActive;

    public Discount() {
    }

    public Discount(String code, Integer percentage, Instant startDate, Instant endDate) {
        this.code = code;
        this.percentage = percentage;
        this.startDate = Instant.now();
        this.endDate = Instant.now();
        this.isActive = true;
        validateDiscount();
    }

    private void validateDiscount() {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Discount code cannot be empty");
        }
        if (percentage == null || percentage <= 0 || percentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 1 and 100");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }

    public boolean isValid() {
        Instant now = Instant.now();

        return isActive
                && !now.isBefore(startDate)
                && !now.isAfter(endDate);
    }


    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
