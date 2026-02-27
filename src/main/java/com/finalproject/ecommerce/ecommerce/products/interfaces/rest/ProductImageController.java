package com.finalproject.ecommerce.ecommerce.products.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductImageCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UploadMultipleProductImagesCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductImage;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductImageCommandService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.mapper.ProductRestMapper;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.mapper.ProductRestMapper.UploadMultipleImagesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/images")
@Tag(name = "Product Images", description = "Endpoints for managing product images")
public class ProductImageController {
    private final ProductImageCommandService productImageCommandService;

    public ProductImageController(ProductImageCommandService productImageCommandService) {
        this.productImageCommandService = productImageCommandService;
    }

    @PostMapping(value = "/{productId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Upload multiple product images")
    public ResponseEntity<UploadMultipleImagesResponse> uploadMultipleProductImages(
            @PathVariable Long productId,
            @Parameter(description = "Image files to upload (3-10 files)", required = true)
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Index of the image to set as primary (0-based, optional)")
            @RequestParam(value = "primaryImageIndex", required = false) Integer primaryImageIndex) {

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body(UploadMultipleImagesResponse.failure("No files provided", 0));
        }
        if (files.size() < 3) {
            return ResponseEntity.badRequest().body(UploadMultipleImagesResponse.failure("At least 3 images are required", files.size()));
        }
        try {
            var command = new UploadMultipleProductImagesCommand(productId, files, primaryImageIndex);
            List<ProductImage> uploadedImages = productImageCommandService.uploadMultipleProductImages(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(ProductRestMapper.toUploadResponse(uploadedImages, files.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(UploadMultipleImagesResponse.failure(e.getMessage(), files.size()));
        }
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Delete product image")
    public ResponseEntity<String> deleteProductImage(@Parameter(description = "Image ID to delete", required = true) @PathVariable Long imageId) {
        productImageCommandService.deleteProductImage(new DeleteProductImageCommand(imageId));
        return ResponseEntity.ok("Image deleted successfully");
    }
}
