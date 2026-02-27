package com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByIsDeletedAndIsActive(Boolean isDeleted, Boolean isActive);

    @EntityGraph(attributePaths = "productCategories")
    List<Product> findDistinctByIdIn(List<Long> ids);

    @EntityGraph(attributePaths = "images")
    List<Product> findByIdIn(List<Long> ids);
}
