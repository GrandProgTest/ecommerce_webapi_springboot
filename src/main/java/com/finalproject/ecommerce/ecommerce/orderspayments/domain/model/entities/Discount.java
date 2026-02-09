package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;

@Entity
@Getter
public class Discount extends AuditableAbstractAggregateRoot<Discount> {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private Integer percentage;

    @Column(nullable = false)
    private Date startDate;

    @Column(nullable = false)
    private Date endDate;

    @Column(nullable = false)
    private Boolean isActive;

    protected Discount() {
    }

    public Discount(String code, Integer percentage, Date startDate, Date endDate) {
        this.code = code;
        this.percentage = percentage;
        this.startDate = startDate;
        this.endDate = endDate;
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
        if (endDate.before(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }

    public boolean isValid() {
        Date now = new Date();
        return isActive && !now.before(startDate) && !now.after(endDate);
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
