package com.medicalstore.pos.config;

import com.medicalstore.pos.entity.User;
import com.medicalstore.pos.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Data initializer for creating default users for each role.
 * In production, remove this or secure it properly.
 */
@Configuration
public class DataInitializer {
    
    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultPassword = passwordEncoder.encode("password123");
            
            // Update or create admin user with new password and role
            userRepository.findByUsername("admin").ifPresentOrElse(
                existingAdmin -> {
                    // Update existing admin to new password and ensure ADMIN role
                    String oldPasswordHash = existingAdmin.getPassword();
                    existingAdmin.setPassword(defaultPassword);
                    existingAdmin.setRole(User.Role.ADMIN);
                    existingAdmin.setActive(true);
                    existingAdmin.setEmail("admin@medicalstore.com");
                    existingAdmin.setFullName("System Administrator");
                    userRepository.save(existingAdmin);
                    System.out.println("============================================");
                    System.out.println("✅ Updated existing admin user");
                    System.out.println("   Username: admin");
                    System.out.println("   Role: ADMIN");
                    System.out.println("============================================");
                },
                () -> {
                    // Create new admin user if doesn't exist
                    User admin = User.builder()
                            .username("admin")
                            .password(defaultPassword)
                            .email("admin@medicalstore.com")
                            .fullName("System Administrator")
                            .role(User.Role.ADMIN)
                            .active(true)
                            .build();
                    userRepository.save(admin);
                    System.out.println("Created new admin user");
                }
            );
            
            // Create or update all other users
            createOrUpdateUser(userRepository, "cashier", defaultPassword, "cashier@medicalstore.com", 
                    "Cashier User", User.Role.CASHIER);
            createOrUpdateUser(userRepository, "stockmonitor", defaultPassword, "stockmonitor@medicalstore.com", 
                    "Stock Monitor", User.Role.STOCK_MONITOR);
            createOrUpdateUser(userRepository, "stockkeeper", defaultPassword, "stockkeeper@medicalstore.com", 
                    "Stock Keeper", User.Role.STOCK_KEEPER);
            createOrUpdateUser(userRepository, "customersupport", defaultPassword, "customersupport@medicalstore.com", 
                    "Customer Support Team", User.Role.CUSTOMER_SUPPORT);
            createOrUpdateUser(userRepository, "analyst", defaultPassword, "analyst@medicalstore.com", 
                    "Data Analyst", User.Role.ANALYST);
            createOrUpdateUser(userRepository, "manager", defaultPassword, "manager@medicalstore.com", 
                    "Manager", User.Role.MANAGER);
            
            System.out.println("============================================");
            System.out.println("✅ All users created/updated successfully!");
            System.out.println("============================================");
            System.out.println();
            System.out.println("Role          | Username          | Access");
            System.out.println("--------------|-------------------|------------------------------------------");
            System.out.println("Admin         | admin             | All pages");
            System.out.println("Cashier       | cashier           | Billing");
            System.out.println("Stock Monitor | stockmonitor      | Inventory");
            System.out.println("Stock Keeper  | stockkeeper       | Medicines");
            System.out.println("Customer Sup. | customersupport   | Returns");
            System.out.println("Analyst       | analyst           | Reports");
            System.out.println("Manager       | manager           | Reports + Purchase History");
            System.out.println("============================================");
            System.out.println("⚠️  IMPORTANT: Change default passwords in production!");
        };
    }
    
    private void createOrUpdateUser(UserRepository userRepository, String username, String password, 
                                   String email, String fullName, User.Role role) {
        userRepository.findByUsername(username).ifPresentOrElse(
            existingUser -> {
                // Update existing user
                existingUser.setPassword(password);
                existingUser.setRole(role);
                existingUser.setActive(true);
                existingUser.setEmail(email);
                existingUser.setFullName(fullName);
                userRepository.save(existingUser);
                System.out.println("✅ Updated user: " + username);
            },
            () -> {
                // Create new user
                User newUser = User.builder()
                        .username(username)
                        .password(password)
                        .email(email)
                        .fullName(fullName)
                        .role(role)
                        .active(true)
                        .build();
                userRepository.save(newUser);
                System.out.println("✅ Created user: " + username);
            }
        );
    }
}



