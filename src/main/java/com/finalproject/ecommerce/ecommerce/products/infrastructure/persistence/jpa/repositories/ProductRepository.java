package com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIsActive(Boolean isActive);

    Optional<Product> findByIdAndIsActive(Long id, Boolean isActive);
}
