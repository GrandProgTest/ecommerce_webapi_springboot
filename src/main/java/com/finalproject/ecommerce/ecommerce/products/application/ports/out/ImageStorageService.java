package com.finalproject.ecommerce.ecommerce.products.application.ports.out;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    String uploadImage(MultipartFile file, Long productId);

    void deleteImage(String imageUrl);

    String extractPublicId(String imageUrl);
}

