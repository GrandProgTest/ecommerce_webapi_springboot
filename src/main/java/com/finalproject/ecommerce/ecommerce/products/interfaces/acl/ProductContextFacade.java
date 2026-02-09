package com.finalproject.ecommerce.ecommerce.products.interfaces.acl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public interface ProductContextFacade {

    boolean productExists(Long productId);

    boolean hasAvailableStock(Long productId, Integer quantity);

    Integer getProductStock(Long productId);

    boolean isProductActive(Long productId);

    BigDecimal getProductPrice(Long productId);

    String getProductName(Long productId);

    /**
     * Gets product names for multiple product IDs in a single query (to avoid N+1 problem)
     *
     * @param productIds List of product IDs
     * @return Map of productId to product name
     */
    Map<Long, String> getProductNames(List<Long> productIds);

    boolean isProductAvailableForPurchase(Long productId, Integer quantity);
}
