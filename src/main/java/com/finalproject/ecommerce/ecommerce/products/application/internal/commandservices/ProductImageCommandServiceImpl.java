package com.finalproject.ecommerce.ecommerce.products.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.products.application.ports.out.ImageStorageService;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.*;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductImageCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UploadMultipleProductImagesCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductImage;
import com.finalproject.ecommerce.ecommerce.products.domain.model.valueobjects.ImageUrl;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductImageCommandService;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductImageRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ProductImageCommandServiceImpl implements ProductImageCommandService {

    private static final int MAX_IMAGES_PER_PRODUCT = 10;
    private static final String[] ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/jpg", "image/png"};
    private static final int MAX_IMAGE_SIZE_MB = 5;

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
    @Transactional
    public List<ProductImage> uploadMultipleProductImages(UploadMultipleProductImagesCommand command) {
        List<MultipartFile> files = command.imageFiles();
        Long productId = command.productId();
        Integer primaryIndex = command.primaryImageIndex();

        for (MultipartFile file : files) {
            validateImageType(file);
            validateImageSize(file);
        }

        int currentImageCount = productImageRepository.findByProductId(productId).size();
        int totalAfterUpload = currentImageCount + files.size();

        if (totalAfterUpload > MAX_IMAGES_PER_PRODUCT) {
            log.warn("Maximum images exceeded for product {}: current={}, new={}, max={}",
                    productId, currentImageCount, files.size(), MAX_IMAGES_PER_PRODUCT);
            throw new MaximumImagesExceededException(
                    productId,
                    totalAfterUpload,
                    MAX_IMAGES_PER_PRODUCT
            );
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        List<ProductImage> uploadedImages = new ArrayList<>();

        if (primaryIndex != null) {
            productImageRepository.findByProductId(productId)
                    .forEach(ProductImage::unsetAsPrimary);
        }

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            boolean isPrimary = primaryIndex != null && i == primaryIndex;

            try {
                String imageUrl = imageStorageService.uploadImage(file, productId);

                ProductImage productImage = new ProductImage(
                        product,
                        new ImageUrl(imageUrl),
                        isPrimary
                );

                ProductImage savedImage = productImageRepository.save(productImage);
                uploadedImages.add(savedImage);

                log.info("Product image {} of {} uploaded: ID={}, ProductID={}, URL={}, isPrimary={}",
                        i + 1, files.size(), savedImage.getId(), productId, imageUrl, isPrimary);

            } catch (Exception e) {
                log.error("Failed to upload image {} of {} for product {}: {}",
                        i + 1, files.size(), productId, e.getMessage(), e);
            }
        }
        return uploadedImages;
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

    private void validateImageSize(MultipartFile file) {
        long maxSizeBytes = MAX_IMAGE_SIZE_MB * 1024L * 1024L;

        if (file.getSize() > maxSizeBytes) {
            log.warn("Image size exceeds limit: {} bytes (max {} bytes)",
                    file.getSize(), maxSizeBytes);

            throw new InvalidImageSizeException(
                    "Image size exceeds " + MAX_IMAGE_SIZE_MB + " MB"
            );
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
