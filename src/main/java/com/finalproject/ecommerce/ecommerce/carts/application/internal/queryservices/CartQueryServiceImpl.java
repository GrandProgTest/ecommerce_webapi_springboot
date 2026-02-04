package com.finalproject.ecommerce.ecommerce.carts.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCurrentUserCartQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.valueobjects.CartStatuses;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartQueryService;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartRepository;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartQueryServiceImpl implements CartQueryService {

    private final CartRepository cartRepository;
    private final IamContextFacade iamContextFacade;

    public CartQueryServiceImpl(CartRepository cartRepository, IamContextFacade iamContextFacade) {
        this.cartRepository = cartRepository;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    public Optional<Cart> handle(GetCartByIdQuery query) {
        return cartRepository.findById(query.cartId());
    }

    @Override
    public Optional<Cart> handle(GetCartByUserIdQuery query) {
        iamContextFacade.validateUserCanAccessResource(query.userId());
        return cartRepository.findByUserIdAndStatus(query.userId(), CartStatuses.ACTIVE);
    }

    @Override
    public Optional<Cart> handle(GetCurrentUserCartQuery query) {
        var userId = iamContextFacade.getCurrentUserId().orElseThrow(() -> new IllegalStateException("User not authenticated"));
        return cartRepository.findByUserIdAndStatus(userId, CartStatuses.ACTIVE);
    }
}
