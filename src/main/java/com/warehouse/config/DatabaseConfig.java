package com.warehouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @Order(1)
    public DataSource dataSource(DataSourceProperties properties) {
        // First, ensure the warehouse database exists
        ensureWarehouseDatabaseExists();

        // Now create a datasource that points to the warehouse database
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/warehouse");
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    private void ensureWarehouseDatabaseExists() {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/postgres", username, password)) {

            boolean databaseExists = false;

            // Check if the warehouse database exists
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(
                        "SELECT 1 FROM pg_database WHERE datname = 'warehouse'");
                databaseExists = resultSet.next();

                if (!databaseExists) {
                    // Create the database if it doesn't exist
                    statement.executeUpdate("CREATE DATABASE warehouse");
                    System.out.println("Database 'warehouse' created successfully.");
                } else {
                    System.out.println("Database 'warehouse' already exists.");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}

