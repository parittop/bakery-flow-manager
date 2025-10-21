package com.pw.bakery.flow.config;

import com.pw.bakery.flow.domain.model.Role;
import com.pw.bakery.flow.domain.model.User;
import com.pw.bakery.flow.repository.RoleRepository;
import com.pw.bakery.flow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    private final Environment environment;

    /**
     * Initialize data for development profile
     */
    @Bean
    @Profile("dev")
    public CommandLineRunner initializeDevelopmentData() {
        return args -> {
            log.info("Initializing development data...");

            try {
                // Create roles
                createRoles();

                // Create users
                createDevelopmentUsers();

                log.info("Development data initialization completed successfully!");

            } catch (Exception e) {
                log.error("Error initializing development data: {}", e.getMessage(), e);
            }
        };
    }

    /**
     * Initialize data for production profile
     */
    @Bean
    @Profile("prod")
    public CommandLineRunner initializeProductionData() {
        return args -> {
            log.info("Initializing production data...");

            try {
                // Create only essential roles
                createRoles();

                // Create admin user only
                createProductionAdmin();

                log.info("Production data initialization completed successfully!");
                log.warn("IMPORTANT: Please change the default admin password immediately!");

            } catch (Exception e) {
                log.error("Error initializing production data: {}", e.getMessage(), e);
            }
        };
    }

    /**
     * Initialize data for test profile (minimal setup)
     */
    @Bean
    @Profile("test")
    public CommandLineRunner initializeTestData() {
        return args -> {
            log.debug("Initializing test data...");

            try {
                // Create only roles for testing
                createRoles();

                log.debug("Test data initialization completed!");

            } catch (Exception e) {
                log.error("Error initializing test data: {}", e.getMessage(), e);
            }
        };
    }

    private void createRoles() {
        if (roleRepository.count() == 0) {
            log.info("Creating default roles...");

            Role[] roles = {
                Role.builder()
                    .name(Role.RoleName.ADMIN)
                    .description(Role.RoleName.ADMIN.getDefaultDescription())
                    .users(new HashSet<>())
                    .build(),
                Role.builder()
                    .name(Role.RoleName.MANAGER)
                    .description(Role.RoleName.MANAGER.getDefaultDescription())
                    .users(new HashSet<>())
                    .build(),
                Role.builder()
                    .name(Role.RoleName.BAKER)
                    .description(Role.RoleName.BAKER.getDefaultDescription())
                    .users(new HashSet<>())
                    .build(),
                Role.builder()
                    .name(Role.RoleName.CASHIER)
                    .description(Role.RoleName.CASHIER.getDefaultDescription())
                    .users(new HashSet<>())
                    .build(),
                Role.builder()
                    .name(Role.RoleName.INVENTORY)
                    .description(Role.RoleName.INVENTORY.getDefaultDescription())
                    .users(new HashSet<>())
                    .build()
            };

            roleRepository.saveAll(Arrays.asList(roles));
            log.info("Created {} default roles", roles.length);

        } else {
            log.info("Roles already exist, skipping role creation");
        }
    }

    private void createDevelopmentUsers() {
        if (userRepository.count() == 0) {
            log.info("Creating development users...");

            // Get roles
            Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));
            Role managerRole = roleRepository.findByName(Role.RoleName.MANAGER)
                .orElseThrow(() -> new RuntimeException("Manager role not found"));
            Role bakerRole = roleRepository.findByName(Role.RoleName.BAKER)
                .orElseThrow(() -> new RuntimeException("Baker role not found"));
            Role cashierRole = roleRepository.findByName(Role.RoleName.CASHIER)
                .orElseThrow(() -> new RuntimeException("Cashier role not found"));
            Role inventoryRole = roleRepository.findByName(Role.RoleName.INVENTORY)
                .orElseThrow(() -> new RuntimeException("Inventory role not found"));

            User[] users = {
                // Admin user
                User.builder()
                    .username("admin")
                    .email("admin@bakery.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .phoneNumber("0812345678")
                    .employeeId("EMP001")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),

                // Manager user
                User.builder()
                    .username("manager")
                    .email("manager@bakery.com")
                    .password(passwordEncoder.encode("manager123"))
                    .firstName("Manager")
                    .lastName("User")
                    .phoneNumber("0823456789")
                    .employeeId("EMP002")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(new HashSet<>(Set.of(managerRole)))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),

                // Baker user
                User.builder()
                    .username("baker")
                    .email("baker@bakery.com")
                    .password(passwordEncoder.encode("baker123"))
                    .firstName("Baker")
                    .lastName("User")
                    .phoneNumber("0834567890")
                    .employeeId("EMP003")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(new HashSet<>(Set.of(bakerRole)))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),

                // Cashier user
                User.builder()
                    .username("cashier")
                    .email("cashier@bakery.com")
                    .password(passwordEncoder.encode("cashier123"))
                    .firstName("Cashier")
                    .lastName("User")
                    .phoneNumber("0845678901")
                    .employeeId("EMP004")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(new HashSet<>(Set.of(cashierRole)))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),

                // Inventory user
                User.builder()
                    .username("inventory")
                    .email("inventory@bakery.com")
                    .password(passwordEncoder.encode("inventory123"))
                    .firstName("Inventory")
                    .lastName("Staff")
                    .phoneNumber("0856789012")
                    .employeeId("EMP005")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(new HashSet<>(Set.of(inventoryRole)))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),

                // Multi-role user (Supervisor)
                User.builder()
                    .username("supervisor")
                    .email("supervisor@bakery.com")
                    .password(passwordEncoder.encode("supervisor123"))
                    .firstName("Supervisor")
                    .lastName("User")
                    .phoneNumber("0867890123")
                    .employeeId("EMP006")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(new HashSet<>(Set.of(managerRole, bakerRole)))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build(),

                // Disabled user for testing
                User.builder()
                    .username("disableduser")
                    .email("disabled@bakery.com")
                    .password(passwordEncoder.encode("disabled123"))
                    .firstName("Disabled")
                    .lastName("User")
                    .phoneNumber("0889012345")
                    .employeeId("EMP008")
                    .enabled(false)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(new HashSet<>(Set.of(cashierRole)))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
            };

            userRepository.saveAll(Arrays.asList(users));
            log.info("Created {} development users", users.length);

            // Log user credentials for development
            log.info("=== Development User Credentials ===");
            for (User user : users) {
                log.info("Username: {}, Password: {}123, Roles: {}",
                    user.getUsername(),
                    user.getUsername().substring(0, user.getUsername().length() - 3),
                    user.getRoles().stream()
                        .map(role -> role.getName().getDisplayName())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("None"));
            }
            log.info("======================================");

        } else {
            log.info("Users already exist, skipping user creation");
        }
    }

    private void createProductionAdmin() {
        if (userRepository.count() == 0) {
            log.info("Creating production admin user...");

            Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User admin = User.builder()
                .username("admin")
                .email("admin@bakery.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("System")
                .lastName("Administrator")
                .phoneNumber("0000000000")
                .employeeId("ADMIN001")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(new HashSet<>(Set.of(adminRole)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            userRepository.save(admin);
            log.info("Created production admin user");
            log.warn("PRODUCTION WARNING: Default admin password is 'admin123'. CHANGE IMMEDIATELY!");

        } else {
            log.info("Users already exist, skipping admin creation");
        }
    }
}
