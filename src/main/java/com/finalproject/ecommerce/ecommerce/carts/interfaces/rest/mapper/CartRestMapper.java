package com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.mapper;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.AddProductToCartCommand;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.ClearCartCommand;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.RemoveCartItemCommand;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.UpdateCartItemQuantityByCartItemIdCommand;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartItem;

import java.time.Instant;
import java.util.List;

public class CartRestMapper {


    public record CartResource(Long id, Long userId, String status, List<CartItemResource> items,
                               Integer totalItems, Instant createdAt, Instant checkedOutAt) {
    }

    public record CartItemResource(Long id, Long productId, Integer quantity) {
    }

    public record AddItemToCartResource(Long productId, Integer quantity) {
    }

    public record UpdateCartItemQuantityResource(Integer quantity) {
    }

    public static CartResource toResource(Cart cart) {
        List<CartItemResource> items = cart.getItems().stream()
                .map(CartRestMapper::toResource)
                .toList();

        return new CartResource(
                cart.getId(),
                cart.getUserId(),
                cart.getStatus().getName(),
                items,
                cart.getTotalItems(),
                cart.getCreatedAt(),
                cart.getCheckedOutAt()
        );
    }

    public static CartItemResource toResource(CartItem item) {
        return new CartItemResource(
                item.getId(),
                item.getProductId(),
                item.getQuantity()
        );
    }


    public static AddProductToCartCommand toAddItemCommand(Long userId, AddItemToCartResource resource) {
        return new AddProductToCartCommand(userId, resource.productId(), resource.quantity());
    }

    public static UpdateCartItemQuantityByCartItemIdCommand toUpdateQuantityCommand(Long userId, Long cartItemId,
                                                                                    UpdateCartItemQuantityResource resource) {
        return new UpdateCartItemQuantityByCartItemIdCommand(userId, cartItemId, resource.quantity());
    }

    public static RemoveCartItemCommand toRemoveItemCommand(Long userId, Long cartItemId) {
        return new RemoveCartItemCommand(userId, cartItemId);
    }

    public static ClearCartCommand toClearCartCommand(Long userId) {
        return new ClearCartCommand(userId);
    }
}

