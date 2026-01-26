package com.finalproject.ecommerce.ecommerce.shared.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;

@Embeddable
@Getter
@Setter
public class DateRange {
    private LocalDate startDate;
    private LocalDate endDate;

    public DateRange() {
    }

    public DateRange(LocalDate startDate, int monthsToAdd) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        this.startDate = startDate;
        this.endDate = startDate.plus(Period.ofMonths(monthsToAdd));
    }
}