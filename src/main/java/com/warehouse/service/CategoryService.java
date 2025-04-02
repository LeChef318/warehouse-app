package com.warehouse.service;

import com.warehouse.dto.CategoryDTO;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.model.Category;
import com.warehouse.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    public CategoryDTO createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category name already exists");
        }
        
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    public CategoryDTO updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        category.setName(categoryDetails.getName());
        
        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new RuntimeException("Cannot delete category with associated products");
        }
        
        categoryRepository.delete(category);
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setProductCount(category.getProducts() != null ? category.getProducts().size() : 0);
        return dto;
    }
}

