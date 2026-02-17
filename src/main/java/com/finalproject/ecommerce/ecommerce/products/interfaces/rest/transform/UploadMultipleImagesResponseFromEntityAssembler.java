package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductImage;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.UploadMultipleImagesResponse;

import java.util.List;
import java.util.stream.Collectors;

public class UploadMultipleImagesResponseFromEntityAssembler {

    public static UploadMultipleImagesResponse toResourceFromEntityList(List<ProductImage> images, int totalFilesAttempted) {
        if (images == null || images.isEmpty()) {
            return UploadMultipleImagesResponse.failure("No images were uploaded successfully", totalFilesAttempted);
        }

        List<UploadMultipleImagesResponse.ImageUploadResult> results = images.stream()
                .map(image -> UploadMultipleImagesResponse.ImageUploadResult.success(
                        image.getId(),
                        image.getUrl(),
                        image.getIsPrimary()
                ))
                .collect(Collectors.toList());

        return images.size() == totalFilesAttempted
                ? UploadMultipleImagesResponse.success(results)
                : UploadMultipleImagesResponse.partial(results);
    }
}

