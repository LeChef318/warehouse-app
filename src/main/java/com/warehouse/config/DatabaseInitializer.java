package com.warehouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    private final ApplicationContext applicationContext;

    public DatabaseInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) throws Exception {
        // Connect to the default 'postgres' database
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

            // Now that we know the database exists, we can switch to using it
            if (applicationContext.getEnvironment() instanceof ConfigurableEnvironment) {
                ConfigurableEnvironment env = (ConfigurableEnvironment) applicationContext.getEnvironment();
                env.addActiveProfile("withdb");
                System.out.println("Switched to 'withdb' profile to use the warehouse database.");
            }

        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

