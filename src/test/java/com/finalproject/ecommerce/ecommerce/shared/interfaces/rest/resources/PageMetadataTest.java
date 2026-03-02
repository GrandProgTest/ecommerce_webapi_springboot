package com.finalproject.ecommerce.ecommerce.shared.interfaces.rest.resources;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageMetadata Tests")
class PageMetadataTest {

    @Test
    @DisplayName("Should create PageMetadata with all fields")
    void shouldCreatePageMetadataWithAllFields() {
        PageMetadata metadata = new PageMetadata(
                0,
                10,
                100L,
                10,
                true,
                false
        );

        assertThat(metadata.currentPage()).isEqualTo(0);
        assertThat(metadata.pageSize()).isEqualTo(10);
        assertThat(metadata.totalElements()).isEqualTo(100L);
        assertThat(metadata.totalPages()).isEqualTo(10);
        assertThat(metadata.hasNext()).isTrue();
        assertThat(metadata.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("Should create PageMetadata for first page")
    void shouldCreatePageMetadataForFirstPage() {
        PageMetadata metadata = new PageMetadata(0, 20, 50L, 3, true, false);

        assertThat(metadata.currentPage()).isEqualTo(0);
        assertThat(metadata.hasNext()).isTrue();
        assertThat(metadata.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("Should create PageMetadata for middle page")
    void shouldCreatePageMetadataForMiddlePage() {
        PageMetadata metadata = new PageMetadata(5, 10, 100L, 10, true, true);

        assertThat(metadata.currentPage()).isEqualTo(5);
        assertThat(metadata.hasNext()).isTrue();
        assertThat(metadata.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("Should create PageMetadata for last page")
    void shouldCreatePageMetadataForLastPage() {
        PageMetadata metadata = new PageMetadata(9, 10, 100L, 10, false, true);

        assertThat(metadata.currentPage()).isEqualTo(9);
        assertThat(metadata.totalPages()).isEqualTo(10);
        assertThat(metadata.hasNext()).isFalse();
        assertThat(metadata.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("Should create PageMetadata for single page")
    void shouldCreatePageMetadataForSinglePage() {
        PageMetadata metadata = new PageMetadata(0, 20, 15L, 1, false, false);

        assertThat(metadata.currentPage()).isEqualTo(0);
        assertThat(metadata.totalPages()).isEqualTo(1);
        assertThat(metadata.totalElements()).isEqualTo(15L);
        assertThat(metadata.hasNext()).isFalse();
        assertThat(metadata.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("Should create PageMetadata for empty result")
    void shouldCreatePageMetadataForEmptyResult() {
        PageMetadata metadata = new PageMetadata(0, 10, 0L, 0, false, false);

        assertThat(metadata.totalElements()).isZero();
        assertThat(metadata.totalPages()).isZero();
        assertThat(metadata.hasNext()).isFalse();
        assertThat(metadata.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("Should support equality comparison")
    void shouldSupportEqualityComparison() {
        PageMetadata metadata1 = new PageMetadata(0, 10, 100L, 10, true, false);
        PageMetadata metadata2 = new PageMetadata(0, 10, 100L, 10, true, false);
        PageMetadata metadata3 = new PageMetadata(1, 10, 100L, 10, true, true);

        assertThat(metadata1).isEqualTo(metadata2);
        assertThat(metadata1).isNotEqualTo(metadata3);
        assertThat(metadata1.hashCode()).isEqualTo(metadata2.hashCode());
    }

    @Test
    @DisplayName("Should have proper toString representation")
    void shouldHaveProperToStringRepresentation() {
        PageMetadata metadata = new PageMetadata(2, 20, 50L, 3, true, true);

        String toString = metadata.toString();

        assertThat(toString).contains("currentPage=2");
        assertThat(toString).contains("pageSize=20");
        assertThat(toString).contains("totalElements=50");
        assertThat(toString).contains("totalPages=3");
        assertThat(toString).contains("hasNext=true");
        assertThat(toString).contains("hasPrevious=true");
    }

    @Test
    @DisplayName("Should handle large page numbers")
    void shouldHandleLargePageNumbers() {
        PageMetadata metadata = new PageMetadata(999, 10, 10000L, 1000, true, true);

        assertThat(metadata.currentPage()).isEqualTo(999);
        assertThat(metadata.totalElements()).isEqualTo(10000L);
        assertThat(metadata.totalPages()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Should handle different page sizes")
    void shouldHandleDifferentPageSizes() {
        PageMetadata small = new PageMetadata(0, 5, 100L, 20, true, false);
        PageMetadata medium = new PageMetadata(0, 20, 100L, 5, true, false);
        PageMetadata large = new PageMetadata(0, 50, 100L, 2, true, false);

        assertThat(small.pageSize()).isEqualTo(5);
        assertThat(small.totalPages()).isEqualTo(20);
        assertThat(medium.pageSize()).isEqualTo(20);
        assertThat(medium.totalPages()).isEqualTo(5);
        assertThat(large.pageSize()).isEqualTo(50);
        assertThat(large.totalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should be immutable record")
    void shouldBeImmutableRecord() {
        PageMetadata metadata = new PageMetadata(0, 10, 100L, 10, true, false);

        assertThat(metadata.currentPage()).isEqualTo(0);
        assertThat(metadata.currentPage()).isEqualTo(0);
        assertThat(metadata).isInstanceOf(Record.class);
    }
}

