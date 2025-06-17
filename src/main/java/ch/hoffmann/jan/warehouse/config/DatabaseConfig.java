package ch.hoffmann.jan.warehouse.config;

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

    // Admin user for database creation and schema management
    @Value("${spring.datasource.admin.username}")
    private String adminUsername;

    @Value("${spring.datasource.admin.password}")
    private String adminPassword;

    // Application user for CRUD operations
    @Value("${spring.datasource.username}")
    private String appUsername;

    @Value("${spring.datasource.password}")
    private String appPassword;

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
        // First, ensure the warehouse database and application user exist
        ensureWarehouseDatabaseAndUserSetup();

        // Now create a datasource that points to the warehouse database using app user
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/warehouse");
        dataSource.setUsername(appUsername);
        dataSource.setPassword(appPassword);

        return dataSource;
    }

    /**
     * Creates a DataSource with admin privileges for database creation
     * This is used only during application startup for database initialization
     */
    @Bean("adminDataSource")
    public DataSource adminDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/warehouse");
        dataSource.setUsername(adminUsername);
        dataSource.setPassword(adminPassword);
        return dataSource;
    }

    private void ensureWarehouseDatabaseAndUserSetup() {
        // Step 1: Ensure database exists
        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/postgres", adminUsername, adminPassword)) {

            boolean databaseExists = false;

            // Check if the warehouse database exists
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(
                        "SELECT 1 FROM pg_database WHERE datname = 'warehouse'");
                databaseExists = resultSet.next();

                if (!databaseExists) {
                    // Create the database if it doesn't exist
                    statement.executeUpdate("CREATE DATABASE warehouse");
                    System.out.println("Database 'warehouse' created successfully using admin user.");
                } else {
                    System.out.println("Database 'warehouse' already exists.");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize database with admin user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }

        // Step 2: Ensure application user exists and has proper permissions
        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/warehouse", adminUsername, adminPassword)) {

            try (Statement statement = connection.createStatement()) {
                
                // Check if application user exists
                ResultSet userExists = statement.executeQuery(
                    "SELECT 1 FROM pg_user WHERE usename = '" + appUsername + "'");
                
                if (!userExists.next()) {
                    // Create application user
                    System.out.println("Creating application user: " + appUsername);
                    statement.executeUpdate(
                        "CREATE USER " + appUsername + " WITH PASSWORD '" + appPassword + "'");
                    System.out.println("Application user created successfully.");
                } else {
                    System.out.println("Application user '" + appUsername + "' already exists.");
                }

                // Grant necessary permissions to application user
                System.out.println("Setting up permissions for application user...");
                
                // Grant connect permission
                statement.executeUpdate("GRANT CONNECT ON DATABASE warehouse TO " + appUsername);
                
                // Grant schema usage
                statement.executeUpdate("GRANT USAGE ON SCHEMA public TO " + appUsername);
                
                // Grant table permissions for existing tables
                statement.executeUpdate(
                    "GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO " + appUsername);
                
                // Grant sequence permissions (needed for auto-increment)
                statement.executeUpdate(
                    "GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO " + appUsername);
                
                // Grant permissions for future tables (important for schema updates)
                statement.executeUpdate(
                    "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO " + appUsername);
                statement.executeUpdate(
                    "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO " + appUsername);
                
                System.out.println("Permissions configured for application user.");

            }
        } catch (Exception e) {
            System.err.println("Failed to setup application user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to setup application user", e);
        }
    }
}

