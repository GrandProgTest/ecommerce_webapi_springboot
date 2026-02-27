package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryStatusRepository extends JpaRepository<DeliveryStatus, Long> {
    Optional<DeliveryStatus> findByName(String name);
}

