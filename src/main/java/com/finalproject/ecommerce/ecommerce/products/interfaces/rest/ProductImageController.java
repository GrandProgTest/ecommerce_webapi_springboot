package com.finalproject.ecommerce.ecommerce.products.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductImageCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UploadProductImageCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductImage;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductImageCommandService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.UploadImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @Operation(summary = "Upload product image", description = "Uploads an image for a product to Cloudinary")
    public ResponseEntity<UploadImageResponse> uploadProductImage(
            @PathVariable Long productId,
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Set as primary image")
            @RequestParam(value = "isPrimary", defaultValue = "false") Boolean isPrimary) {

        UploadProductImageCommand command = new UploadProductImageCommand(
                productId,
                file,
                isPrimary
        );
        ProductImage uploadedImage = productImageCommandService.uploadProductImage(command);
        UploadImageResponse response = UploadImageResponse.success(
                uploadedImage.getId(),
                uploadedImage.getUrl(),
                uploadedImage.getIsPrimary()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Delete product image", description = "Deletes a product image from database and Cloudinary")
    public ResponseEntity<String> deleteProductImage(
            @Parameter(description = "Image ID to delete", required = true)
            @PathVariable Long imageId) {

        DeleteProductImageCommand command = new DeleteProductImageCommand(imageId);
        productImageCommandService.deleteProductImage(command);
        return ResponseEntity.ok("Image deleted successfully");
    }
}
