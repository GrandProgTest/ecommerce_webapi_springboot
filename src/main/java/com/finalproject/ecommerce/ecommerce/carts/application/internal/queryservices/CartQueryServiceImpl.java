package com.finalproject.ecommerce.ecommerce.carts.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.valueobjects.CartStatuses;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartQueryService;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartQueryServiceImpl implements CartQueryService {

    private final CartRepository cartRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> handle(GetCartByIdQuery query) {
        return cartRepository.findById(query.cartId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> handle(GetCartByUserIdQuery query) {
        return cartRepository.findByUserIdAndStatus(query.userId(), CartStatuses.ACTIVE);
    }
}
