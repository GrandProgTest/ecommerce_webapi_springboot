package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCurrentUserCartQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartQueryService;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources.CartGraphQLResource;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.transform.CartGraphQLResourceFromEntityAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CartQueryResolver {

    private final CartQueryService cartQueryService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public CartGraphQLResource currentUserCart() {
        var query = new GetCurrentUserCartQuery();
        return cartQueryService.handle(query)
                .map(CartGraphQLResourceFromEntityAssembler::toResourceFromEntity)
                .orElse(null);
    }
}
