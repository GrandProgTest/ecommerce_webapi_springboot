package com.finalproject.ecommerce.ecommerce.products.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.products.application.ports.out.ImageStorageService;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.InvalidImageTypeException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.MaximumImagesExceededException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductImageNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductImageCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UploadProductImageCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductImage;
import com.finalproject.ecommerce.ecommerce.products.domain.model.valueobjects.ImageUrl;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductImageCommandService;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductImageRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ProductImageCommandServiceImpl implements ProductImageCommandService {

    private static final int MAX_IMAGES_PER_PRODUCT = 10;
    private static final String[] ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/jpg", "image/png"};

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ImageStorageService imageStorageService;

    public ProductImageCommandServiceImpl(
            ProductImageRepository productImageRepository,
            ProductRepository productRepository,
            ImageStorageService imageStorageService) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
        this.imageStorageService = imageStorageService;
    }

    @Override
    public ProductImage uploadProductImage(UploadProductImageCommand command) {
        validateImageType(command.imageFile());
        validateMaxImages(command.productId());

        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));
        String imageUrl = imageStorageService.uploadImage(command.imageFile(), command.productId());
        if (command.isPrimary()) {
            productImageRepository.findByProductId(command.productId())
                    .forEach(ProductImage::unsetAsPrimary);
        }
        ProductImage productImage = new ProductImage(
                product,
                new ImageUrl(imageUrl),
                command.isPrimary()
        );
        ProductImage savedImage = productImageRepository.save(productImage);
        log.info("Product image uploaded and saved: ID={}, ProductID={}, URL={}",
                savedImage.getId(), command.productId(), imageUrl);
        return savedImage;
    }

    @Override
    public void deleteProductImage(DeleteProductImageCommand command) {
        ProductImage productImage = productImageRepository.findById(command.imageId())
                .orElseThrow(() -> new ProductImageNotFoundException(command.imageId()));
        String imageUrl = productImage.getUrl();
        try {
            imageStorageService.deleteImage(imageUrl);
        } catch (Exception e) {
            log.error("Failed to delete image from Cloudinary: {}", imageUrl, e);
        }
        productImageRepository.delete(productImage);
        log.info("Product image deleted: ID={}, URL={}", command.imageId(), imageUrl);
    }

    private void validateImageType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new InvalidImageTypeException("unknown");
        }

        boolean isValid = false;
        for (String allowedType : ALLOWED_CONTENT_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            log.warn("Invalid image type attempted: {}", contentType);
            throw new InvalidImageTypeException(contentType);
        }
    }

    private void validateMaxImages(Long productId) {
        int currentImageCount = productImageRepository.findByProductId(productId).size();
        if (currentImageCount >= MAX_IMAGES_PER_PRODUCT) {
            log.warn("Maximum images exceeded for product {}: current={}, max={}",
                    productId, currentImageCount, MAX_IMAGES_PER_PRODUCT);
            throw new MaximumImagesExceededException(productId, currentImageCount, MAX_IMAGES_PER_PRODUCT);
        }
    }
}
