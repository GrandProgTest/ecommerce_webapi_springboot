package com.finalproject.ecommerce.ecommerce.carts.domain.services;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByUserIdQuery;

import java.util.Optional;

public interface CartQueryService {

    Optional<Cart> handle(GetCartByIdQuery query);
    Optional<Cart> handle(GetCartByUserIdQuery query);
}
