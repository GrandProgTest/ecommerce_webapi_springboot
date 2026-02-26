package com.finalproject.ecommerce.ecommerce.products.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.notifications.interfaces.acl.NotificationContextFacade;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.acl.OrdersContextFacade;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.CategoryNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.DuplicateCategoryAssignmentException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductInOrdersException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductPriceLog;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductCommandService;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductCategoryRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductPriceLogRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final IamContextFacade iamContextFacade;
    private final OrdersContextFacade ordersContextFacade;
    private final NotificationContextFacade notificationContextFacade;
    private final ProductPriceLogRepository productPriceLogRepository;

    public ProductCommandServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ProductCategoryRepository productCategoryRepository, ProductPriceLogRepository productPriceLogRepository, IamContextFacade iamContextFacade, OrdersContextFacade ordersContextFacade, NotificationContextFacade notificationContextFacade) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productPriceLogRepository = productPriceLogRepository;
        this.iamContextFacade = iamContextFacade;
        this.ordersContextFacade = ordersContextFacade;
        this.notificationContextFacade = notificationContextFacade;
    }

    @Override
    public Long handle(CreateProductCommand command) {
        var userId = iamContextFacade.getCurrentUserId().orElseThrow(() -> new IllegalStateException("User not authenticated"));

        if (command.categoryIds() == null || command.categoryIds().isEmpty()) {
            throw new IllegalArgumentException("At least one category is required");
        }

        var product = new Product(command, userId);

        for (Long categoryId : command.categoryIds()) {
            var category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId));
            product.assignCategory(category);
        }

        try {
            productRepository.save(product);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error saving product: %s".formatted(e.getMessage()));
        }
        return product.getId();
    }

    @Override
    public Optional<Product> handle(UpdateProductCommand command) {
        var result = productRepository.findById(command.productId());
        if (result.isEmpty()) throw new ProductNotFoundException(command.productId());
        var productToUpdate = result.get();
        try {
            BigDecimal oldPrice = productToUpdate.getPrice();

            productToUpdate.updateProductInfo(command.name(), command.description(), command.price(), command.stock());
            var updatedProduct = productRepository.save(productToUpdate);

            if (command.price() != null && oldPrice.compareTo(command.price()) != 0) {
                var priceLog = ProductPriceLog.basePriceChange(
                        updatedProduct.getId(), oldPrice, command.price()
                );
                productPriceLogRepository.save(priceLog);
            }

            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while updating product: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public void handle(DeleteProductCommand command) {
        if (!productRepository.existsById(command.productId())) {
            throw new ProductNotFoundException(command.productId());
        }

        if (ordersContextFacade.productExistsInOrders(command.productId())) {
            throw new ProductInOrdersException(command.productId());
        }

        try {
            productRepository.deleteById(command.productId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while deleting product: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public Optional<Product> handle(AssignCategoryToProductCommand command) {
        if (!productRepository.existsById(command.productId())) {
            throw new ProductNotFoundException(command.productId());
        }

        var category = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(command.categoryId()));

        if (productCategoryRepository.existsByProduct_IdAndCategory_Id(command.productId(), command.categoryId())) {
            throw new DuplicateCategoryAssignmentException(command.productId(), command.categoryId());
        }

        try {
            return productRepository.findById(command.productId()).map(product -> {
                product.assignCategory(category);
                productRepository.save(product);
                return product;
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while assigning category to product: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public boolean handle(ToggleProductLikeCommand command) {
        final Long userId;
        if (command.userId() == null) {
            userId = iamContextFacade.getCurrentUserId()
                    .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        } else {
            iamContextFacade.validateUserCanAccessResource(command.userId());
            userId = command.userId();
        }

        var product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        try {
            boolean isLiked = product.toggleLike(userId);
            productRepository.save(product);
            return isLiked;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while toggling product like: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public Optional<Product> handle(DecreaseProductStockCommand command) {
        var product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        try {
            product.decreaseStock(command.quantity());
            var updatedProduct = productRepository.save(product);
            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while decreasing product stock: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public Optional<Product> handle(IncreaseProductStockCommand command) {
        var product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        try {
            product.increaseStock(command.quantity());
            var updatedProduct = productRepository.save(product);
            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while increasing product stock: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public Optional<Product> handle(ActivateProductCommand command) {
        var product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        try {
            product.activate();
            var updatedProduct = productRepository.save(product);
            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while activating product: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public Optional<Product> handle(DeactivateProductCommand command) {
        var product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        try {
            product.deactivate();
            var updatedProduct = productRepository.save(product);
            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while deactivating product: %s".formatted(e.getMessage()));
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "productById", key = "#command.productId()"),
            @CacheEvict(value = "allProducts", allEntries = true),
            @CacheEvict(value = "activeProducts", allEntries = true),
            @CacheEvict(value = "productsPage", allEntries = true),
            @CacheEvict(value = "productsPageGraphQL", allEntries = true),
            @CacheEvict(value = "productsByCategory", allEntries = true),
            @CacheEvict(value = "productsByIds", allEntries = true)
    })
    public Optional<Product> handle(SoftDeleteProductCommand command) {
        var product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        if (ordersContextFacade.productExistsInOrders(command.productId())) {
            throw new ProductInOrdersException(command.productId());
        }

        try {
            product.softDelete();
            var updatedProduct = productRepository.save(product);
            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while soft deleting product: %s".formatted(e.getMessage()));
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "productById", key = "#command.productId()"),
            @CacheEvict(value = "allProducts", allEntries = true),
            @CacheEvict(value = "activeProducts", allEntries = true),
            @CacheEvict(value = "productsPage", allEntries = true),
            @CacheEvict(value = "productsPageGraphQL", allEntries = true),
            @CacheEvict(value = "productsByCategory", allEntries = true),
            @CacheEvict(value = "productsByIds", allEntries = true)
    })
    public Optional<Product> handle(SetProductSalePriceCommand command) {
        var product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        try {
            product.setSalePrice(command.salePrice(), command.salePriceExpireDate());
            var updatedProduct = productRepository.save(product);

            var salePriceLog = ProductPriceLog.salePriceChange(
                    product.getId(),
                    product.getPrice(),
                    command.salePrice(),
                    command.salePriceExpireDate()
            );
            productPriceLogRepository.save(salePriceLog);

            if (command.salePrice() != null) {
                notifyUsersOfDiscount(updatedProduct, command.salePrice(), command.salePriceExpireDate());
            }

            return Optional.of(updatedProduct);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while setting product sale price: %s".formatted(e.getMessage()));
        }
    }

    private void notifyUsersOfDiscount(Product product, BigDecimal salePrice, java.time.Instant salePriceExpireDate) {
        List<Long> userIds = product.getLikedByUserIds();
        if (userIds.isEmpty()) {
            return;
        }

        Map<Long, String> userEmails = iamContextFacade.getUserEmails(userIds);
        if (userEmails.isEmpty()) {
            return;
        }

        Set<String> recipientEmails = new HashSet<>(userEmails.values());

        BigDecimal originalPrice = product.getPrice();
        BigDecimal savings = originalPrice.subtract(salePrice);
        BigDecimal savingsPercent = savings.multiply(BigDecimal.valueOf(100))
                .divide(originalPrice, 0, RoundingMode.HALF_UP);

        String expireDateStr = salePriceExpireDate != null
                ? salePriceExpireDate.toString()
                : "Limited time";

        notificationContextFacade.sendDiscountAlertBatch(
                recipientEmails,
                product.getName(),
                originalPrice.toPlainString(),
                salePrice.toPlainString(),
                savingsPercent.toPlainString(),
                expireDateStr
        );
    }
}
