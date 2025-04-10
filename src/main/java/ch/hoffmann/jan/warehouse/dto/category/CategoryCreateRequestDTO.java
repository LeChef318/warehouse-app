package ch.hoffmann.jan.warehouse.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryCreateRequestDTO {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    private String name;

    // Constructors
    public CategoryCreateRequestDTO() {
    }

    public CategoryCreateRequestDTO(String name) {
        this.name = name;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CategoryCreateRequestDTO{" +
                "name='" + name + '\'' +
                '}';
    }
}