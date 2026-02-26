package com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByIsDeleted(Boolean isDeleted);

    List<Product> findByIsDeletedAndIsActive(Boolean isDeleted, Boolean isActive);

    Page<Product> findDistinctByIsDeletedAndProductCategories_Category_Id(Boolean isDeleted, Long categoryId, Pageable pageable);

    Page<Product> findDistinctByIsDeletedAndIsActiveAndProductCategories_Category_Id(Boolean isDeleted, Boolean isActive, Long categoryId, Pageable pageable);
}
