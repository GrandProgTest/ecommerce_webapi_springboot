package com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.resources;

import java.util.Date;
import java.util.List;

public record CartResource(
    Long id,
    Long userId,
    String status,
    List<CartItemResource> items,
    Integer totalItems,
    Date createdAt,
    Date checkedOutAt
) {
}
