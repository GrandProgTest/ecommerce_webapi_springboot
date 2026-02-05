package com.finalproject.ecommerce.ecommerce.orderspayments.repositories;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Optional<Discount> findByCode(String code);

    boolean existsByCode(String code);
}
