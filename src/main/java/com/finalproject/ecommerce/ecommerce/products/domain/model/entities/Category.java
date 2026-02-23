package com.finalproject.ecommerce.ecommerce.products.domain.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Category extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private final List<ProductCategory> productCategories = new ArrayList<>();

    public Category() {
    }

    public Category(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        if (name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException("Category name must be between 2 and 100 characters");
        }
        this.name = name;
    }

    public void updateName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        if (newName.length() < 2 || newName.length() > 100) {
            throw new IllegalArgumentException("Category name must be between 2 and 100 characters");
        }
        this.name = newName;
    }
}
