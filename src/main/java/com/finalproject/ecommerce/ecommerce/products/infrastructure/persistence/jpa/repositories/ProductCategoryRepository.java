package com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    boolean existsByProduct_IdAndCategory_Id(Long productId, Long categoryId);

    List<ProductCategory> findByProduct_Id(Long productId);

    List<ProductCategory> findByCategory_Id(Long categoryId);

    Optional<ProductCategory> findByProduct_IdAndCategory_Id(Long productId, Long categoryId);

    void deleteByProduct_Id(Long productId);
}
