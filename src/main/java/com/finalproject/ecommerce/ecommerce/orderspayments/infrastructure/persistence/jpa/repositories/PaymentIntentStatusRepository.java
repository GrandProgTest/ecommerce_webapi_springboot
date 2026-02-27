package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.PaymentIntentStatus;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.PaymentIntentStatuses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentIntentStatusRepository extends JpaRepository<PaymentIntentStatus, Long> {

    Optional<PaymentIntentStatus> findByName(PaymentIntentStatuses name);

    boolean existsByName(PaymentIntentStatuses name);
}

