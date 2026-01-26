package com.finalproject.ecommerce.ecommerce.products.repositories;

import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, ProductLike.ProductLikeId> {

    List<ProductLike> findByProductId(Long productId);

    List<ProductLike> findByUserId(Long userId);

    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    long countByProductId(Long productId);
}
