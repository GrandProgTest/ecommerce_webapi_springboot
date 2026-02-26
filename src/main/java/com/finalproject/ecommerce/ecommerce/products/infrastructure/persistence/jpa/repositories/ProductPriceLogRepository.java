package com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductPriceLog;
import com.finalproject.ecommerce.ecommerce.products.domain.model.valueobjects.PriceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductPriceLogRepository extends JpaRepository<ProductPriceLog, Long> {
    List<ProductPriceLog> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<ProductPriceLog> findByProductIdAndPriceTypeOrderByCreatedAtDesc(Long productId, PriceType priceType);
}
