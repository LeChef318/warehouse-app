package ch.hoffmann.jan.warehouse.dto.warehouse;

import java.util.List;

public class WarehouseResponseDTO {
    private Long id;
    private String name;
    private String location;
    private List<WarehouseStockDTO> stocks;

    // Constructors
    public WarehouseResponseDTO() {
    }

    public WarehouseResponseDTO(Long id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<WarehouseStockDTO> getStocks() {
        return stocks;
    }

    public void setStocks(List<WarehouseStockDTO> stocks) {
        this.stocks = stocks;
    }

    @Override
    public String toString() {
        return "WarehouseResponseDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", stockCount=" + (stocks != null ? stocks.size() : 0) +
                '}';
    }
}