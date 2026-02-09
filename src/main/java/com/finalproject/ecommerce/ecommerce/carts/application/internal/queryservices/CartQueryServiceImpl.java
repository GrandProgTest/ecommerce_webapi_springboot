package com.finalproject.ecommerce.ecommerce.carts.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartStatus;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCurrentUserCartQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.valueobjects.CartStatuses;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartQueryService;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartRepository;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartStatusRepository;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartQueryServiceImpl implements CartQueryService {

    private final CartRepository cartRepository;
    private final CartStatusRepository cartStatusRepository;
    private final IamContextFacade iamContextFacade;

    public CartQueryServiceImpl(CartRepository cartRepository, CartStatusRepository cartStatusRepository, IamContextFacade iamContextFacade) {
        this.cartRepository = cartRepository;
        this.cartStatusRepository = cartStatusRepository;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    public Optional<Cart> handle(GetCartByIdQuery query) {
        return cartRepository.findById(query.cartId());
    }

    @Override
    public Optional<Cart> handle(GetCartByUserIdQuery query) {
        iamContextFacade.validateUserCanAccessResource(query.userId());
        CartStatus activeStatus = cartStatusRepository.findByName(CartStatuses.ACTIVE.name()).orElseThrow(() -> new IllegalStateException("Active cart status not found"));
        return cartRepository.findByUserIdAndStatus(query.userId(), activeStatus);
    }

    @Override
    public Optional<Cart> handle(GetCurrentUserCartQuery query) {
        var userId = iamContextFacade.getCurrentUserId().orElseThrow(() -> new IllegalStateException("User not authenticated"));
        CartStatus activeStatus = cartStatusRepository.findByName(CartStatuses.ACTIVE.name()).orElseThrow(() -> new IllegalStateException("Active cart status not found"));
        return cartRepository.findByUserIdAndStatus(userId, activeStatus);
    }
}
