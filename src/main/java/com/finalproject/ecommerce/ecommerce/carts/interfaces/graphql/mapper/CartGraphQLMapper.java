package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.mapper;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.AddProductToCartCommand;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.ClearCartCommand;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.RemoveCartItemCommand;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.UpdateCartItemQuantityByCartItemIdCommand;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartItem;

import java.time.Instant;
import java.util.List;

public class CartGraphQLMapper {


    public record CartGraphQLResource(Long id, Long userId, String status, List<CartItemGraphQLResource> items,
                                      Integer totalItems, Instant createdAt, Instant updatedAt, Instant checkedOutAt) {
    }

    public record CartItemGraphQLResource(Long id, Long productId, Integer quantity, Instant createdAt,
                                          Instant updatedAt) {
    }

    public record UpdateCartItemQuantityGraphQLInput(Integer quantity) {
    }

    public static CartGraphQLResource toResource(Cart cart) {
        List<CartItemGraphQLResource> items = cart.getItems().stream()
                .map(CartGraphQLMapper::toResource)
                .toList();

        return new CartGraphQLResource(
                cart.getId(),
                cart.getUserId(),
                cart.getStatus().getName(),
                items,
                cart.getTotalItems(),
                cart.getCreatedAt(),
                cart.getUpdatedAt(),
                cart.getCheckedOutAt()
        );
    }

    public static CartItemGraphQLResource toResource(CartItem item) {
        return new CartItemGraphQLResource(
                item.getId(),
                item.getProductId(),
                item.getQuantity(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }


    public static AddProductToCartCommand toAddItemCommand(Long userId, Long productId, Integer quantity) {
        return new AddProductToCartCommand(userId, productId, quantity);
    }

    public static UpdateCartItemQuantityByCartItemIdCommand toUpdateQuantityCommand(Long userId, Long cartItemId,
                                                                                    Integer quantity) {
        return new UpdateCartItemQuantityByCartItemIdCommand(userId, cartItemId, quantity);
    }

    public static RemoveCartItemCommand toRemoveItemCommand(Long userId, Long cartItemId) {
        return new RemoveCartItemCommand(userId, cartItemId);
    }

    public static ClearCartCommand toClearCartCommand(Long userId) {
        return new ClearCartCommand(userId);
    }
}
