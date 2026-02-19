package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.graphql.resources.OrderUserGraphQLResource;

public class OrderUserGraphQLResourceFromEntityAssembler {

    public static OrderUserGraphQLResource toResourceFromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new OrderUserGraphQLResource(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail()
        );
    }
}

