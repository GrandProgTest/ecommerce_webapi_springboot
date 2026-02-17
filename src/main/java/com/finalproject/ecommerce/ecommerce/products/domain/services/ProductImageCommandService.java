package com.finalproject.ecommerce.ecommerce.products.domain.services;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductImageCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UploadMultipleProductImagesCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductImage;

import java.util.List;

public interface ProductImageCommandService {
    List<ProductImage> uploadMultipleProductImages(UploadMultipleProductImagesCommand command);

    void deleteProductImage(DeleteProductImageCommand command);
}
