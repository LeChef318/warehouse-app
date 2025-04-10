package ch.hoffmann.jan.warehouse.service;

import ch.hoffmann.jan.warehouse.dto.category.CategoryCreateRequestDTO;
import ch.hoffmann.jan.warehouse.dto.category.CategoryResponseDTO;
import ch.hoffmann.jan.warehouse.exception.WarehouseException;
import ch.hoffmann.jan.warehouse.model.Category;
import ch.hoffmann.jan.warehouse.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::convertToResponseDTO)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Category", "id", id));
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryCreateRequestDTO createRequest) {
        // Check if category with the same name already exists
        if (categoryRepository.existsByName(createRequest.getName())) {
            throw new WarehouseException.DuplicateResourceException("Category", "name", createRequest.getName());
        }

        // Create and save the new category
        Category category = new Category();
        category.setName(createRequest.getName());

        Category savedCategory = categoryRepository.save(category);
        return convertToResponseDTO(savedCategory);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryCreateRequestDTO updateRequest) {
        // Find the category to update
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Category", "id", id));

        if (!Objects.equals(category.getName(), updateRequest.getName())) {
            // Check if another category with the new name already exists
            if (categoryRepository.existsByName(updateRequest.getName())) {
                throw new WarehouseException.DuplicateResourceException("Category", "name", updateRequest.getName());
            }

            category.setName(updateRequest.getName());
        }

        Category updatedCategory = categoryRepository.save(category);
        return convertToResponseDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        // Find the category to delete
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Category", "id", id));

        // Check if the category has associated products
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new WarehouseException.CategoryInUseException(category.getName(), category.getProducts().size());
        }

        // Delete the category
        categoryRepository.delete(category);
    }

    /**
     * Converts a Category entity to a CategoryResponseDTO
     */
    private CategoryResponseDTO convertToResponseDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setProductCount(category.getProducts() != null ? category.getProducts().size() : 0);
        return dto;
    }
}