package com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto;

import java.util.List;

public record CartDto(Long id, Long userId, boolean isActive, List<CartItemDto> items) {
}
