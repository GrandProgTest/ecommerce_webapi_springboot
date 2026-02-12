package com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    List<ProductLike> findByProduct(Product product);

    List<ProductLike> findByUserId(Long userId);

    Optional<ProductLike> findByUserIdAndProduct(Long userId, Product product);

    boolean existsByUserIdAndProduct(Long userId, Product product);

    long countByProduct(Product product);
}
