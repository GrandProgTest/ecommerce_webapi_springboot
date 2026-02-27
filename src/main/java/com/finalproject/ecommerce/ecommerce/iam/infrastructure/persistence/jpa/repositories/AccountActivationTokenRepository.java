package com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.UserToken;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountActivationTokenRepository extends JpaRepository<UserToken, Long> {

    Optional<UserToken> findByUser_IdAndIsUsedFalse(Long userId);

    List<UserToken> findByUser_IdAndTokenTypeAndIsUsedFalse(Long userId, TokenType tokenType);

    List<UserToken> findByTokenTypeAndIsUsedFalse(TokenType tokenType);
}

