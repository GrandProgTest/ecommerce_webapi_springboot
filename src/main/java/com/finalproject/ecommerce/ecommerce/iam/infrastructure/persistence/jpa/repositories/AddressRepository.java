package com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);

    Optional<Address> findByUserAndIsDefaultTrue(User user);

    boolean existsByIdAndUser(Long id, User user);
}
