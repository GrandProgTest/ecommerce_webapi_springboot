package com.finalproject.ecommerce.ecommerce.products.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.Category;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetAllCategoriesQuery;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryQueryServiceImpl Unit Tests")
class CategoryQueryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryQueryServiceImpl categoryQueryService;
    private Category electronicsCategory;

    @BeforeEach
    void setUp() {
        electronicsCategory = new Category("Electronics");
    }

    @Test
    @DisplayName("Should return all categories")
    void shouldReturnAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(electronicsCategory));
        List<Category> result = categoryQueryService.handle(new GetAllCategoriesQuery());
        assertThat(result).hasSize(1);
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no categories")
    void shouldReturnEmptyList() {
        when(categoryRepository.findAll()).thenReturn(new ArrayList<>());
        List<Category> result = categoryQueryService.handle(new GetAllCategoriesQuery());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find category by ID")
    void shouldFindCategoryById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronicsCategory));
        Optional<Category> result = categoryQueryService.findById(1L);
        assertThat(result).isPresent();
        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when not found")
    void shouldReturnEmptyWhenNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<Category> result = categoryQueryService.findById(999L);
        assertThat(result).isEmpty();
    }
}