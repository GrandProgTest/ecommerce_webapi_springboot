package com.finalproject.ecommerce.ecommerce.carts.domain.services;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByUserIdQuery;

import java.util.Optional;

public interface CartCommandService {

    Cart handle(AddProductToCartCommand command);

    Cart handle(UpdateCartItemQuantityCommand command);

    Cart handle(UpdateCartItemQuantityByCartItemIdCommand command);

    Cart handle(RemoveProductFromCartCommand command);

    Cart handle(RemoveCartItemCommand command);

    Cart handle(ClearCartCommand command);

    Cart handle(CheckoutCartCommand command);

    void handle(SeedCartStatusCommand command);
}
