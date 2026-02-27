package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCurrentUserCartQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartQueryService;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.mapper.CartGraphQLMapper;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.mapper.CartGraphQLMapper.CartGraphQLResource;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class CartQueryResolver {

    private final CartQueryService cartQueryService;

    public CartQueryResolver(CartQueryService cartQueryService) {
        this.cartQueryService = cartQueryService;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public CartGraphQLResource getCurrentUserCart() {
        var query = new GetCurrentUserCartQuery();
        return cartQueryService.handle(query)
                .map(CartGraphQLMapper::toResource)
                .orElse(null);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public CartGraphQLResource getCartByUserId(@Argument String userId) {
        var query = new GetCartByUserIdQuery(Long.parseLong(userId));
        return cartQueryService.handle(query)
                .map(CartGraphQLMapper::toResource)
                .orElse(null);
    }
}
