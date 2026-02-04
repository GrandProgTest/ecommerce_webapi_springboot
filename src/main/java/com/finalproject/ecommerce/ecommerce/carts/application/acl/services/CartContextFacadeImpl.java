package com.finalproject.ecommerce.ecommerce.carts.application.acl.services;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartQueryService;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.CartContextFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartContextFacadeImpl implements CartContextFacade {

    private final CartQueryService cartQueryService;

    @Override
    public Optional<Cart> getCartById(Long cartId) {
        var query = new GetCartByIdQuery(cartId);
        return cartQueryService.handle(query);
    }

    @Override
    public Optional<Cart> getActiveCartByUserId(Long userId) {
        var query = new GetCartByUserIdQuery(userId);
        return cartQueryService.handle(query);
    }
}
