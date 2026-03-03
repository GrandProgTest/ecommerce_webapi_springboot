package com.finalproject.ecommerce.ecommerce.products.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.products.application.ports.out.ImageStorageService;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.*;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductImageCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UploadMultipleProductImagesCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductImage;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductImageRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductImageCommandServiceImpl Unit Tests")
class ProductImageCommandServiceImplTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private ProductImageCommandServiceImpl productImageCommandService;

    private Product testProduct;
    private MockMultipartFile validImage;

    @BeforeEach
    void setUp() {
        CreateProductCommand cmd = new CreateProductCommand(
                "Test Product",
                "Description",
                new BigDecimal("100.00"),
                50,
                List.of(1L),
                true
        );
        testProduct = new Product(cmd, 1L);

        byte[] imageContent = new byte[1024 * 1024];
        validImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                imageContent
        );
    }

    @Nested
    @DisplayName("Upload Multiple Product Images")
    class UploadMultipleProductImagesTests {

        @Test
        @DisplayName("Should upload single image successfully")
        void shouldUploadSingleImageSuccessfully() {
            Long productId = 1L;
            String imageUrl = "https://cloudinary.com/image1.jpg";

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productImageRepository.findByProduct_Id(productId)).thenReturn(new ArrayList<>());
            when(imageStorageService.uploadImage(any(MultipartFile.class), eq(productId)))
                    .thenReturn(imageUrl);
            when(productImageRepository.save(any(ProductImage.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UploadMultipleProductImagesCommand command = new UploadMultipleProductImagesCommand(
                    productId,
                    List.of(validImage),
                    null
            );

            List<ProductImage> result = productImageCommandService.uploadMultipleProductImages(command);

            assertThat(result).hasSize(1);
            verify(productImageRepository).save(any(ProductImage.class));
            verify(imageStorageService).uploadImage(validImage, productId);
        }

        @Test
        @DisplayName("Should upload multiple images successfully")
        void shouldUploadMultipleImagesSuccessfully() {
            Long productId = 1L;
            MockMultipartFile image2 = new MockMultipartFile(
                    "image2",
                    "test2.png",
                    "image/png",
                    new byte[1024 * 1024]
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productImageRepository.findByProduct_Id(productId)).thenReturn(new ArrayList<>());
            when(imageStorageService.uploadImage(any(MultipartFile.class), eq(productId)))
                    .thenReturn("https://cloudinary.com/image.jpg");
            when(productImageRepository.save(any(ProductImage.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UploadMultipleProductImagesCommand command = new UploadMultipleProductImagesCommand(
                    productId,
                    List.of(validImage, image2),
                    null
            );

            List<ProductImage> result = productImageCommandService.uploadMultipleProductImages(command);

            assertThat(result).hasSize(2);
            verify(productImageRepository, times(2)).save(any(ProductImage.class));
            verify(imageStorageService, times(2)).uploadImage(any(MultipartFile.class), eq(productId));
        }

        @Test
        @DisplayName("Should set primary image when index is provided")
        void shouldSetPrimaryImageWhenIndexProvided() {
            Long productId = 1L;
            Integer primaryIndex = 0;

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productImageRepository.findByProduct_Id(productId)).thenReturn(new ArrayList<>());
            when(imageStorageService.uploadImage(any(MultipartFile.class), eq(productId)))
                    .thenReturn("https://cloudinary.com/image.jpg");
            when(productImageRepository.save(any(ProductImage.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UploadMultipleProductImagesCommand command = new UploadMultipleProductImagesCommand(
                    productId,
                    List.of(validImage),
                    primaryIndex
            );

            List<ProductImage> result = productImageCommandService.uploadMultipleProductImages(command);

            assertThat(result).hasSize(1);
            verify(productImageRepository, times(2)).findByProduct_Id(productId);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            Long productId = 999L;

            when(productImageRepository.findByProduct_Id(productId)).thenReturn(new ArrayList<>());
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            UploadMultipleProductImagesCommand command = new UploadMultipleProductImagesCommand(
                    productId,
                    List.of(validImage),
                    null
            );

            assertThatThrownBy(() -> productImageCommandService.uploadMultipleProductImages(command))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Should throw exception when maximum images exceeded")
        void shouldThrowExceptionWhenMaximumImagesExceeded() {
            Long productId = 1L;
            List<ProductImage> existingImages = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                existingImages.add(mock(ProductImage.class));
            }

            when(productImageRepository.findByProduct_Id(productId)).thenReturn(existingImages);

            UploadMultipleProductImagesCommand command = new UploadMultipleProductImagesCommand(
                    productId,
                    List.of(validImage),
                    null
            );

            assertThatThrownBy(() -> productImageCommandService.uploadMultipleProductImages(command))
                    .isInstanceOf(MaximumImagesExceededException.class);
        }

        @Test
        @DisplayName("Should throw exception for invalid image type")
        void shouldThrowExceptionForInvalidImageType() {
            Long productId = 1L;
            MockMultipartFile invalidImage = new MockMultipartFile(
                    "image",
                    "test.pdf",
                    "application/pdf",
                    new byte[1024]
            );

            UploadMultipleProductImagesCommand command = new UploadMultipleProductImagesCommand(
                    productId,
                    List.of(invalidImage),
                    null
            );

            assertThatThrownBy(() -> productImageCommandService.uploadMultipleProductImages(command))
                    .isInstanceOf(InvalidImageTypeException.class)
                    .hasMessageContaining("application/pdf");
        }

        @Test
        @DisplayName("Should throw exception when image type is null")
        void shouldThrowExceptionWhenImageTypeIsNull() {
            Long productId = 1L;
            MockMultipartFile imageWithNullType = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    null,
                    new byte[1024]
            );

            UploadMultipleProductImagesCommand command = new UploadMultipleProductImagesCommand(
                    productId,
                    List.of(imageWithNullType),
                    null
            );

            assertThatThrownBy(() -> productImageCommandService.uploadMultipleProductImages(command))
                    .isInstanceOf(InvalidImageTypeException.class)
                    .hasMessageContaining("unknown");
        }

        @Test
        @DisplayName("Should throw exception when image size exceeds limit")
        void shouldThrowExceptionWhenImageSizeExceedsLimit() {
            Long productId = 1L;
            byte[] largeContent = new byte[6 * 1024 * 1024];
            MockMultipartFile largeImage = new MockMultipartFile(
                    "image",
                    "large.jpg",
                    "image/jpeg",
                    largeContent
            );

            UploadMultipleProductImagesCommand command = new UploadMultipleProductImagesCommand(
                    productId,
                    List.of(largeImage),
                    null
            );

            assertThatThrownBy(() -> productImageCommandService.uploadMultipleProductImages(command))
                    .isInstanceOf(InvalidImageSizeException.class)
                    .hasMessageContaining("5 MB");
        }

        @Test
        @DisplayName("Should accept JPG images")
        void shouldAcceptJpgImages() {
            Long productId = 1L;
            MockMultipartFile jpgImage = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    "image/jpg",
                    new byte[1024]
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productImageRepository.findByProduct_Id(productId)).thenReturn(new ArrayList<>());
            when(imageStorageService.uploadImage(any(MultipartFile.class), eq(productId)))
                    .thenReturn("https://cloudinary.com/image.jpg");
            when(productImageRepository.save(any(ProductImage.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UploadMultipleProductImagesCommand command = new UploadMultipleProductImagesCommand(
                    productId,
                    List.of(jpgImage),
                    null
            );

            List<ProductImage> result = productImageCommandService.uploadMultipleProductImages(command);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should accept PNG images")
        void shouldAcceptPngImages() {
            Long productId = 1L;
            MockMultipartFile pngImage = new MockMultipartFile(
                    "image",
                    "test.png",
                    "image/png",
                    new byte[1024]
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productImageRepository.findByProduct_Id(productId)).thenReturn(new ArrayList<>());
            when(imageStorageService.uploadImage(any(MultipartFile.class), eq(productId)))
                    .thenReturn("https://cloudinary.com/image.png");
            when(productImageRepository.save(any(ProductImage.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UploadMultipleProductImagesCommand command = new UploadMultipleProductImagesCommand(
                    productId,
                    List.of(pngImage),
                    null
            );

            List<ProductImage> result = productImageCommandService.uploadMultipleProductImages(command);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should handle upload failure gracefully")
        void shouldHandleUploadFailureGracefully() {
            Long productId = 1L;

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productImageRepository.findByProduct_Id(productId)).thenReturn(new ArrayList<>());
            when(imageStorageService.uploadImage(any(MultipartFile.class), eq(productId)))
                    .thenThrow(new RuntimeException("Upload failed"));

            UploadMultipleProductImagesCommand command = new UploadMultipleProductImagesCommand(
                    productId,
                    List.of(validImage),
                    null
            );

            List<ProductImage> result = productImageCommandService.uploadMultipleProductImages(command);

            assertThat(result).isEmpty();
            verify(productImageRepository, never()).save(any(ProductImage.class));
        }
    }

    @Nested
    @DisplayName("Delete Product Image")
    class DeleteProductImageTests {

        @Test
        @DisplayName("Should delete product image successfully")
        void shouldDeleteProductImageSuccessfully() {
            Long imageId = 1L;
            String imageUrl = "https://cloudinary.com/image.jpg";
            ProductImage productImage = mock(ProductImage.class);

            when(productImage.getUrl()).thenReturn(imageUrl);
            when(productImageRepository.findById(imageId)).thenReturn(Optional.of(productImage));
            doNothing().when(imageStorageService).deleteImage(imageUrl);
            doNothing().when(productImageRepository).delete(productImage);

            DeleteProductImageCommand command = new DeleteProductImageCommand(imageId);

            productImageCommandService.deleteProductImage(command);

            verify(imageStorageService).deleteImage(imageUrl);
            verify(productImageRepository).delete(productImage);
        }

        @Test
        @DisplayName("Should throw exception when image not found")
        void shouldThrowExceptionWhenImageNotFound() {
            Long imageId = 999L;

            when(productImageRepository.findById(imageId)).thenReturn(Optional.empty());

            DeleteProductImageCommand command = new DeleteProductImageCommand(imageId);

            assertThatThrownBy(() -> productImageCommandService.deleteProductImage(command))
                    .isInstanceOf(ProductImageNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Should delete from repository even if cloudinary deletion fails")
        void shouldDeleteFromRepositoryEvenIfCloudinaryDeletionFails() {
            Long imageId = 1L;
            String imageUrl = "https://cloudinary.com/image.jpg";
            ProductImage productImage = mock(ProductImage.class);

            when(productImage.getUrl()).thenReturn(imageUrl);
            when(productImageRepository.findById(imageId)).thenReturn(Optional.of(productImage));
            doThrow(new RuntimeException("Cloudinary error")).when(imageStorageService).deleteImage(imageUrl);
            doNothing().when(productImageRepository).delete(productImage);

            DeleteProductImageCommand command = new DeleteProductImageCommand(imageId);

            productImageCommandService.deleteProductImage(command);

            verify(imageStorageService).deleteImage(imageUrl);
            verify(productImageRepository).delete(productImage);
        }
    }

    @Nested
    @DisplayName("Image Validation")
    class ImageValidationTests {

        @Test
        @DisplayName("Should validate multiple images before upload")
        void shouldValidateMultipleImagesBeforeUpload() {
            Long productId = 1L;
            MockMultipartFile invalidImage = new MockMultipartFile(
                    "image",
                    "test.gif",
                    "image/gif",
                    new byte[1024]
            );

            UploadMultipleProductImagesCommand command = new UploadMultipleProductImagesCommand(
                    productId,
                    List.of(validImage, invalidImage),
                    null
            );

            assertThatThrownBy(() -> productImageCommandService.uploadMultipleProductImages(command))
                    .isInstanceOf(InvalidImageTypeException.class);

            verify(imageStorageService, never()).uploadImage(any(), anyLong());
        }

        @Test
        @DisplayName("Should check total image count before upload")
        void shouldCheckTotalImageCountBeforeUpload() {
            Long productId = 1L;
            List<ProductImage> existingImages = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                existingImages.add(mock(ProductImage.class));
            }

            List<MultipartFile> newImages = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                newImages.add(validImage);
            }

            when(productImageRepository.findByProduct_Id(productId)).thenReturn(existingImages);

            UploadMultipleProductImagesCommand command = new UploadMultipleProductImagesCommand(
                    productId,
                    newImages,
                    null
            );

            assertThatThrownBy(() -> productImageCommandService.uploadMultipleProductImages(command))
                    .isInstanceOf(MaximumImagesExceededException.class);
        }
    }
}






