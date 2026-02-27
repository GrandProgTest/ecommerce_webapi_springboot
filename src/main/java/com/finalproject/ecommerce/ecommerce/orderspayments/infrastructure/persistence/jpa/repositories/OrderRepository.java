package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findByUserId(Long userId, Pageable pageable);

    Optional<Order> findByCartId(Long cartId);

    Optional<Order> findByStripePaymentIntentId(String stripePaymentIntentId);

    boolean existsByItemsProductIdAndStatusName(Long productId, String statusName);
}

