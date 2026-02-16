package com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.AccountActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountActivationTokenRepository extends JpaRepository<AccountActivationToken, Long> {

    Optional<AccountActivationToken> findByHashedToken(String hashedToken);

    Optional<AccountActivationToken> findByUser_IdAndIsUsedFalse(Long userId);

    void deleteByUser_Id(Long userId);
}

