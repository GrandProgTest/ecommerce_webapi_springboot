package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartCommandService;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources.*;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.transform.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class CartMutationResolver {

    private final CartCommandService cartCommandService;

    public CartMutationResolver(CartCommandService cartCommandService){
        this.cartCommandService = cartCommandService;
    }
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CartGraphQLResource addItemToCart(@Argument Long userId, @Argument Long productId, @Argument Integer quantity) {
        var resource = new AddItemToCartGraphQLResource(userId, productId, quantity);
        var command = AddItemToCartCommandFromResourceAssembler.toCommandFromResource(resource);
        var cart = cartCommandService.handle(command);

        return CartGraphQLResourceFromEntityAssembler.toResourceFromEntity(cart);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CartGraphQLResource updateCartItemQuantity(
            @Argument Long userId,
            @Argument Long cartItemId,
            @Argument UpdateCartItemQuantityGraphQLInput input) {

        var resource = new UpdateCartItemQuantityGraphQLResource(userId, cartItemId, input.quantity());
        var command = UpdateCartItemQuantityCommandFromResourceAssembler.toCommandFromResource(resource);
        var cart = cartCommandService.handle(command);

        return CartGraphQLResourceFromEntityAssembler.toResourceFromEntity(cart);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CartGraphQLResource removeItemFromCart(@Argument Long userId, @Argument Long cartItemId) {
        var input = new RemoveItemFromCartGraphQLInput(userId, cartItemId);
        var command = RemoveCartItemCommandFromResourceAssembler.toCommandFromResource(input);
        var cart = cartCommandService.handle(command);

        return CartGraphQLResourceFromEntityAssembler.toResourceFromEntity(cart);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CartGraphQLResource clearCart(@Argument Long userId) {
        var input = new ClearCartGraphQLInput(userId);
        var command = ClearCartCommandFromResourceAssembler.toCommandFromResource(input);
        var cart = cartCommandService.handle(command);

        return CartGraphQLResourceFromEntityAssembler.toResourceFromEntity(cart);
    }
}
