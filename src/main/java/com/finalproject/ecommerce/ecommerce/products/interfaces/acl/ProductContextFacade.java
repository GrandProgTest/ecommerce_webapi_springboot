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

    Map<Long, String> getProductNames(List<Long> productIds);

    boolean isProductAvailableForPurchase(Long productId, Integer quantity);
}
