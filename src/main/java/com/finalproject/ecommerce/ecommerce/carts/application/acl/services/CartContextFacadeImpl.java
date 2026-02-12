package com.finalproject.ecommerce.ecommerce.carts.application.acl.services;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.CheckoutCartCommand;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartCommandService;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartQueryService;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.CartContextFacade;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto.CartDto;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto.CartItemDto;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartContextFacadeImpl implements CartContextFacade {

    private final CartQueryService cartQueryService;
    private final CartCommandService cartCommandService;

    public CartContextFacadeImpl(CartQueryService cartQueryService, CartCommandService cartCommandService) {
        this.cartQueryService = cartQueryService;
        this.cartCommandService = cartCommandService;
    }

    @Override
    public Optional<CartDto> getCartById(Long cartId) {
        var query = new GetCartByIdQuery(cartId);
        return cartQueryService.handle(query).map(this::toDto);
    }

    @Override
    public Optional<CartDto> getActiveCartByUserId(Long userId) {
        var query = new GetCartByUserIdQuery(userId);
        return cartQueryService.handle(query).map(this::toDto);
    }

    @Override
    public void checkoutCart(Long userId, Long cartId) {
        var command = new CheckoutCartCommand(userId);
        cartCommandService.handle(command);
    }

    private CartDto toDto(Cart cart) {
        var itemDtos = cart.getItems().stream().map(item -> new CartItemDto(item.getId(), item.getProductId(), item.getQuantity())).collect(Collectors.toList());

        return new CartDto(cart.getId(), cart.getUserId(), cart.isActive(), itemDtos);
    }
}
