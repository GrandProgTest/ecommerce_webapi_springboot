package com.finalproject.ecommerce.ecommerce.carts.application.acl.services;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartQueryService;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.CartContextFacade;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto.CartDto;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto.CartItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartContextFacadeImpl implements CartContextFacade {

    private final CartQueryService cartQueryService;

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

    private CartDto toDto(Cart cart) {
        var itemDtos = cart.getItems().stream().map(item -> new CartItemDto(item.getId(), item.getProductId(), item.getQuantity())).collect(Collectors.toList());

        return new CartDto(cart.getId(), cart.getUserId(), cart.isActive(), itemDtos);
    }
}
