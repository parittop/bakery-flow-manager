package com.pw.bakery.flow.config;

import com.pw.bakery.flow.domain.model.Role;
import com.pw.bakery.flow.domain.model.User;
import com.pw.bakery.flow.repository.RoleRepository;
import com.pw.bakery.flow.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data initializer component for seeding initial data
 * Runs automatically on application startup for specific profiles
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Initialize data for test profile
     */
    @Bean
    @Profile("test")
    @Transactional
    public CommandLineRunner initializeTestData() {
        return args -> {
            log.info("Initializing test data...");

            try {
                // Create roles first
                createRoles();
                log.info("Roles created successfully");

                // Wait a moment for transaction to commit
                Thread.sleep(100);

                // Create test admin user
                createTestAdminUser();
                log.info("Test admin user created successfully");

                // Verify user was created
                boolean userExists = userRepository.existsByUsername("admin");
                log.info("Admin user verification - exists: {}", userExists);

                log.info("Test data initialization completed!");
            } catch (Exception e) {
                log.error(
                    "Error initializing test data: {}",
                    e.getMessage(),
                    e
                );
                throw new RuntimeException("Failed to initialize test data", e);
            }
        };
    }

    /**
     * Create default roles
     */
    @Transactional
    private void createRoles() {
        try {
            if (roleRepository.count() == 0) {
                log.info("Creating default roles...");

                // Create roles one by one to avoid lazy loading issues
                Role adminRole = Role.builder()
                    .name(Role.RoleName.ADMIN)
                    .description(Role.RoleName.ADMIN.getDefaultDescription())
                    .build();

                Role managerRole = Role.builder()
                    .name(Role.RoleName.MANAGER)
                    .description(Role.RoleName.MANAGER.getDefaultDescription())
                    .build();

                Role bakerRole = Role.builder()
                    .name(Role.RoleName.BAKER)
                    .description(Role.RoleName.BAKER.getDefaultDescription())
                    .build();

                Role cashierRole = Role.builder()
                    .name(Role.RoleName.CASHIER)
                    .description(Role.RoleName.CASHIER.getDefaultDescription())
                    .build();

                Role inventoryRole = Role.builder()
                    .name(Role.RoleName.INVENTORY)
                    .description(
                        Role.RoleName.INVENTORY.getDefaultDescription()
                    )
                    .build();

                roleRepository.save(adminRole);
                roleRepository.save(managerRole);
                roleRepository.save(bakerRole);
                roleRepository.save(cashierRole);
                roleRepository.save(inventoryRole);

                log.info("Created 5 default roles");
            } else {
                log.info("Roles already exist, skipping role creation");
            }
        } catch (Exception e) {
            log.error("Error creating roles: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Create test admin user
     */
    @Transactional
    private void createTestAdminUser() {
        try {
            if (userRepository.count() == 0) {
                log.info("Creating test admin user...");

                Role adminRole = roleRepository
                    .findByName(Role.RoleName.ADMIN)
                    .orElseThrow(() ->
                        new RuntimeException("Admin role not found")
                    );

                User admin = User.builder()
                    .username("admin")
                    .email("admin@bakery.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .employeeId("ADMIN001")
                    .phoneNumber("0812345678")
                    .enabled(true)
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

                userRepository.save(admin);
                log.info("Created test admin user: admin/admin123");

                // Verify the user was actually saved
                User savedUser = userRepository
                    .findByUsername("admin")
                    .orElse(null);
                if (savedUser != null) {
                    log.info("✅ Admin user successfully saved to database");
                } else {
                    log.error("❌ Failed to save admin user to database");
                }
            } else {
                log.info("Test users already exist, skipping user creation");
            }
        } catch (Exception e) {
            log.error("Error creating test admin user: {}", e.getMessage(), e);
            throw e;
        }
    }
}
