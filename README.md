# Warehouse Management System

A Java Spring Boot application for managing warehouse inventory across multiple locations.

## Features

- User Management (Login/Registration)
- OAuth2 Security with Keycloak
- Role-based Access Control (Employee/Manager)
- Product Management (CRUD operations)
- Category Management
- Warehouse Management
- Stock Management across multiple warehouses
- Audit Logging
- Swagger UI for API Documentation

## Technology Stack

- Java 23
- Spring Boot 3.2.3
- Spring Security with OAuth2
- Spring Data JPA
- PostgreSQL
- Swagger/OpenAPI
- Maven

## Database Structure

- **User**: id, username, password, role
- **Product**: id, name, description, price, category_id
- **Category**: id, name
- **Warehouse**: id, name, location
- **Stock**: id, product_id, warehouse_id, quantity
- **Audit**: id, user_id, action, product_id, warehouse_id, target_warehouse_id, quantity, timestamp

## API Endpoints

### User Controller
- `GET /api/users` - Get all users (Manager only)
- `GET /api/users/{id}` - Get user by ID (Manager only)
- `GET /api/users/me` - Get current user
- `POST /api/users/register` - Register a new user
- `PATCH /api/users/{id}` - Update user (Manager only)
- `PATCH /api/users/me` - Update current user
- `PUT /api/users/{id}/promote` - Promote user to manager (Manager only)
- `PUT /api/users/{id}/demote` - Demote user to employee (Manager only)
- `DELETE /api/users/{id}` - Delete user (Manager only)

### Category Controller
- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create category (Manager only)
- `PUT /api/categories/{id}` - Update category (Manager only)
- `DELETE /api/categories/{id}` - Delete category (Manager only)

### Product Controller
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/category/{categoryId}` - Get products by category
- `POST /api/products` - Create product (Manager only)
- `PUT /api/products/{id}` - Update product (Manager only)
- `DELETE /api/products/{id}` - Delete product (Manager only)

### Warehouse Controller
- `GET /api/warehouses` - Get all warehouses
- `GET /api/warehouses/{id}` - Get warehouse by ID
- `POST /api/warehouses` - Create warehouse (Manager only)
- `PUT /api/warehouses/{id}` - Update warehouse (Manager only)
- `DELETE /api/warehouses/{id}` - Delete warehouse (Manager only)

### Stock Controller
- `GET /api/stocks` - Get all stocks
- `GET /api/stocks/product/{productId}` - Get stocks by product
- `GET /api/stocks/warehouse/{warehouseId}` - Get stocks by warehouse
- `GET /api/stocks/product/{productId}/warehouse/{warehouseId}` - Get stock by product and warehouse
- `POST /api/stocks` - Create stock (Manager only)
- `PUT /api/stocks` - Update stock (Manager only)
- `POST /api/stocks/transfer` - Transfer stock from one warehouse to another (Manager only)

### Audit Controller
- `GET /api/audit` - Get all audit logs (Manager only)
- `GET /api/audit/recent` - Get recent audit logs (Manager only)

## Setup and Installation

1. Clone the repository
2. Configure PostgreSQL database
3. Configure Keycloak for OAuth2 authentication
4. Update application.properties with your database and Keycloak settings
5. Run the application using Maven: `mvn spring-boot:run`
6. Access Swagger UI at: http://localhost:8080/swagger-ui.html

## Keycloak Configuration

1. Create a new realm called "warehouse"
2. Create client with client ID "warehouse-app"
3. Configure client access type as "confidential"
4. Create roles: "EMPLOYEE" and "MANAGER"
5. Create users and assign roles

## License

This project is licensed under the MIT License - see the LICENSE file for details.

