package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects;

import java.util.Set;

public enum DeliveryStatuses {
    PACKED, SHIPPED, IN_TRANSIT, DELIVERED;

    public Set<DeliveryStatuses> validNextStatuses() {
        return switch (this) {
            case PACKED -> Set.of(SHIPPED);
            case SHIPPED -> Set.of(IN_TRANSIT);
            case IN_TRANSIT -> Set.of(DELIVERED);
            case DELIVERED -> Set.of();
        };
    }

    public boolean canTransitionTo(DeliveryStatuses target) {
        return validNextStatuses().contains(target);
    }

    public static Set<DeliveryStatuses> validInitialStatuses() {
        return Set.of(PACKED);
    }
}

