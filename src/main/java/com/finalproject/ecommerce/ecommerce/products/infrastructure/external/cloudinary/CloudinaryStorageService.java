package com.finalproject.ecommerce.ecommerce.products.infrastructure.external.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.finalproject.ecommerce.ecommerce.products.application.ports.out.ImageStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryStorageService implements ImageStorageService {
    private final Cloudinary cloudinary;

    public CloudinaryStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadImage(MultipartFile file, Long productId) {
        try {
            String folder = "ecommerce/products/" + productId;
            String timestamp = String.valueOf(System.currentTimeMillis());
            String publicId = "product_" + productId + "_" + timestamp;

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "public_id", publicId,
                            "resource_type", "image",
                            "tags", new String[]{"product", "product_" + productId, "ecommerce"},
                            "context", "product_id=" + productId + "|uploaded_at=" + timestamp,
                            "transformation", new com.cloudinary.Transformation()
                                    .width(1000).height(1000).crop("limit").quality("auto")
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully to Cloudinary: {} for product ID: {}", secureUrl, productId);
            return secureUrl;
        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary for product {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        try {
            String publicId = extractPublicId(imageUrl);
            Map deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String result = (String) deleteResult.get("result");
            if ("ok".equals(result)) {
                log.info("Image deleted successfully from Cloudinary: {}", publicId);
            } else {
                log.warn("Image deletion returned status: {} for publicId: {}", result, publicId);
            }
        } catch (IOException e) {
            log.error("Error deleting image from Cloudinary: {}", imageUrl, e);
            throw new RuntimeException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        try {
            int uploadIndex = imageUrl.indexOf("/upload/");
            if (uploadIndex == -1) {
                throw new IllegalArgumentException("Invalid Cloudinary URL format");
            }
            String afterUpload = imageUrl.substring(uploadIndex + 8);
            int versionIndex = afterUpload.indexOf("/v");
            if (versionIndex != -1) {
                afterUpload = afterUpload.substring(versionIndex + 1);
                int slashAfterVersion = afterUpload.indexOf("/");
                if (slashAfterVersion != -1) {
                    afterUpload = afterUpload.substring(slashAfterVersion + 1);
                }
            }
            int lastDotIndex = afterUpload.lastIndexOf(".");
            if (lastDotIndex != -1) {
                afterUpload = afterUpload.substring(0, lastDotIndex);
            }
            return afterUpload;
        } catch (Exception e) {
            log.error("Error extracting public ID from URL: {}", imageUrl, e);
            throw new IllegalArgumentException("Could not extract public ID from URL: " + imageUrl, e);
        }
    }
}
