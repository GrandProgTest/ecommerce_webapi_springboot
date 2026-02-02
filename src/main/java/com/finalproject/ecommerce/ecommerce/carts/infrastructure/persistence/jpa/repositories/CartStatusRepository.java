package com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartStatusRepository extends JpaRepository<CartStatus, String> {

    boolean existsByName(String name);
}
