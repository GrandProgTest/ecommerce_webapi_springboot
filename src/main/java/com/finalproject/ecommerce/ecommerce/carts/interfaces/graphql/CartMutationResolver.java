package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartCommandService;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources.AddItemToCartGraphQLResource;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources.CartGraphQLResource;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.transform.AddItemToCartCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.transform.CartGraphQLResourceFromEntityAssembler;
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
    public CartGraphQLResource addItemToCart(@Argument Long productId, @Argument Integer quantity) {
        var resource = new AddItemToCartGraphQLResource(productId, quantity);
        var command = AddItemToCartCommandFromResourceAssembler.toCommandFromResource(resource);
        var cart = cartCommandService.handle(command);

        return CartGraphQLResourceFromEntityAssembler.toResourceFromEntity(cart);
    }
}
