package com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductSalePriceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSalePriceLogRepository extends JpaRepository<ProductSalePriceLog, Long> {
    List<ProductSalePriceLog> findByProductIdOrderByCreatedAtDesc(Long productId);
}

