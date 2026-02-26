package com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountActivationTokenRepository extends JpaRepository<UserToken, Long> {

    Optional<UserToken> findByHashedToken(String hashedToken);

    Optional<UserToken> findByUser_IdAndIsUsedFalse(Long userId);

    void deleteByUser_Id(Long userId);
}

