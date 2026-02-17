package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;

import java.util.List;

public record UploadMultipleImagesResponse(String status, String message, int uploadedCount, int totalFiles,
                                           List<ImageUploadResult> images) {

    public record ImageUploadResult(Long imageId, String imageUrl, Boolean isPrimary, boolean success,
                                    String errorMessage) {
        public static ImageUploadResult success(Long imageId, String imageUrl, Boolean isPrimary) {
            return new ImageUploadResult(imageId, imageUrl, isPrimary, true, null);
        }

        public static ImageUploadResult failure(String errorMessage) {
            return new ImageUploadResult(null, null, false, false, errorMessage);
        }
    }

    public static UploadMultipleImagesResponse success(List<ImageUploadResult> images) {
        long successCount = images.stream().filter(ImageUploadResult::success).count();
        return new UploadMultipleImagesResponse("SUCCESS", "Uploaded " + successCount + " of " + images.size() + " images", (int) successCount, images.size(), images);
    }

    public static UploadMultipleImagesResponse partial(List<ImageUploadResult> images) {
        long successCount = images.stream().filter(ImageUploadResult::success).count();
        return new UploadMultipleImagesResponse("PARTIAL_SUCCESS", "Uploaded " + successCount + " of " + images.size() + " images successfully", (int) successCount, images.size(), images);
    }

    public static UploadMultipleImagesResponse failure(String message, int totalFiles) {
        return new UploadMultipleImagesResponse("FAILURE", message, 0, totalFiles, List.of());
    }
}

