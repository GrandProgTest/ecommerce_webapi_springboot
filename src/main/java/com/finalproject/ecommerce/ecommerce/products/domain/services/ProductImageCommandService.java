package com.finalproject.ecommerce.ecommerce.products.domain.services;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductImageCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UploadProductImageCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductImage;

public interface ProductImageCommandService {
    ProductImage uploadProductImage(UploadProductImageCommand command);

    void deleteProductImage(DeleteProductImageCommand command);
}
