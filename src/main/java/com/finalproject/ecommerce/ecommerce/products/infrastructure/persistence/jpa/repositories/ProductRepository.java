package com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIsActive(Boolean isActive);

    Optional<Product> findByIdAndIsActive(Long id, Boolean isActive);

    Page<Product> findByIsActive(Boolean isActive, Pageable pageable);

    List<Product> findByIsDeleted(Boolean isDeleted);

    Page<Product> findByIsDeleted(Boolean isDeleted, Pageable pageable);

    List<Product> findByIsDeletedAndIsActive(Boolean isDeleted, Boolean isActive);

    Page<Product> findByIsDeletedAndIsActive(Boolean isDeleted, Boolean isActive, Pageable pageable);

    Page<Product> findDistinctByIsDeletedAndProductCategories_Category_Id(Boolean isDeleted, Long categoryId, Pageable pageable);

    Page<Product> findDistinctByIsDeletedAndIsActiveAndProductCategories_Category_Id(Boolean isDeleted, Boolean isActive, Long categoryId, Pageable pageable);
}
