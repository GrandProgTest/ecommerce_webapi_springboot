package com.finalproject.ecommerce.ecommerce.products.interfaces.acl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public interface ProductContextFacade {

    Integer getProductStock(Long productId);

    boolean isProductActive(Long productId);

    boolean isProductDeleted(Long productId);

    BigDecimal getProductPrice(Long productId);

    String getProductName(Long productId);

    Map<Long, String> getProductNames(List<Long> productIds);

    void decreaseProductStock(Long productId, Integer quantity);

    void increaseProductStock(Long productId, Integer quantity);

    List<Long> getUsersWhoLikedProduct(Long productId);

    boolean hasActiveSalePrice(Long productId);
}
