package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {

    Optional<PaymentIntent> findByStripePaymentIntentId(String stripePaymentIntentId);

    Optional<PaymentIntent> findByOrderId(Long orderId);

    boolean existsByStripePaymentIntentId(String stripePaymentIntentId);
}

