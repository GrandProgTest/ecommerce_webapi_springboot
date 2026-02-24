package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartCommandService;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.mapper.CartGraphQLMapper;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.mapper.CartGraphQLMapper.CartGraphQLResource;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.mapper.CartGraphQLMapper.UpdateCartItemQuantityGraphQLInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class CartMutationResolver {

    private final CartCommandService cartCommandService;

    public CartMutationResolver(CartCommandService cartCommandService) {
        this.cartCommandService = cartCommandService;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CartGraphQLResource addItemToCart(@Argument Long userId, @Argument Long productId, @Argument Integer quantity) {
        var command = CartGraphQLMapper.toAddItemCommand(userId, productId, quantity);
        var cart = cartCommandService.handle(command);
        return CartGraphQLMapper.toResource(cart);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CartGraphQLResource updateCartItemQuantity(
            @Argument Long userId,
            @Argument Long cartItemId,
            @Argument UpdateCartItemQuantityGraphQLInput input) {

        var command = CartGraphQLMapper.toUpdateQuantityCommand(userId, cartItemId, input.quantity());
        var cart = cartCommandService.handle(command);
        return CartGraphQLMapper.toResource(cart);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CartGraphQLResource removeItemFromCart(@Argument Long userId, @Argument Long cartItemId) {
        var command = CartGraphQLMapper.toRemoveItemCommand(userId, cartItemId);
        var cart = cartCommandService.handle(command);
        return CartGraphQLMapper.toResource(cart);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CartGraphQLResource clearCart(@Argument Long userId) {
        var command = CartGraphQLMapper.toClearCartCommand(userId);
        var cart = cartCommandService.handle(command);
        return CartGraphQLMapper.toResource(cart);
    }
}
