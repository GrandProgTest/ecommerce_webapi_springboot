package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    Optional<Order> findByCartId(Long cartId);
}
