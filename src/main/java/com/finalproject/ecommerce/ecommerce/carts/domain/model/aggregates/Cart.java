package com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Cart extends AuditableAbstractAggregateRoot<Cart> {
}
