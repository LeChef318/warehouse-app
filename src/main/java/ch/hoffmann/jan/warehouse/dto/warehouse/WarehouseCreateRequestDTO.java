package ch.hoffmann.jan.warehouse.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WarehouseCreateRequestDTO {

    @NotBlank(message = "Warehouse name is required")
    @Size(min = 2, max = 100, message = "Warehouse name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 200, message = "Location must be between 2 and 200 characters")
    private String location;

    // Constructors
    public WarehouseCreateRequestDTO() {
    }

    public WarehouseCreateRequestDTO(String name, String location) {
        this.name = name;
        this.location = location;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "WarehouseCreateRequestDTO{" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}