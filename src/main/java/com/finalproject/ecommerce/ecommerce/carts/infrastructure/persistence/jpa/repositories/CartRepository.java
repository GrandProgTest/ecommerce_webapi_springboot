package com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.valueobjects.CartStatuses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatuses status);

    Optional<Cart> findByUserId(Long userId);

    boolean existsByUserIdAndStatus(Long userId, CartStatuses status);
}
