package com.pw.bakery.flow.config;

import com.pw.bakery.flow.domain.model.Role;
import com.pw.bakery.flow.domain.model.User;
import com.pw.bakery.flow.repository.RoleRepository;
import com.pw.bakery.flow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for DataInitializer component
 * Tests data initialization for different profiles
 */
@SpringBootTest
@ActiveProfiles("test")
class DataInitializerTest {

    @Autowired
    private DataInitializer dataInitializer;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private Environment environment;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(roleRepository, userRepository, passwordEncoder, environment);
    }

    @Test
    void testCreateRolesWhenEmpty() {
        // Given
        when(roleRepository.count()).thenReturn(0L);
        when(roleRepository.findByName(Role.RoleName.ADMIN)).thenReturn(Optional.empty());
        when(roleRepository.findByName(Role.RoleName.MANAGER)).thenReturn(Optional.empty());
        when(roleRepository.findByName(Role.RoleName.BAKER)).thenReturn(Optional.empty());
        when(roleRepository.findByName(Role.RoleName.CASHIER)).thenReturn(Optional.empty());
        when(roleRepository.findByName(Role.RoleName.INVENTORY)).thenReturn(Optional.empty());
        when(roleRepository.saveAll(any())).thenReturn(Arrays.asList());

        // When
        dataInitializer.createRoles();

        // Then
        verify(roleRepository).count();
        verify(roleRepository, times(5)).findByName(any());
        verify(roleRepository).saveAll(any());
    }

    @Test
    void testCreateRolesWhenAlreadyExist() {
        // Given
        when(roleRepository.count()).thenReturn(5L);

        // When
        dataInitializer.createRoles();

        // Then
        verify(roleRepository).count();
        verify(roleRepository, never()).findByName(any());
        verify(roleRepository, never()).saveAll(any());
    }

    @Test
    void testCreateDevelopmentUsersWhenEmpty() {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");

        // Mock roles
        Role adminRole = Role.builder()
                .name(Role.RoleName.ADMIN)
                .description("Full system access")
                .build();
        Role managerRole = Role.builder()
                .name(Role.RoleName.MANAGER)
                .description("Operations manager")
                .build();
        Role bakerRole = Role.builder()
                .name(Role.RoleName.BAKER)
                .description("Production baker")
                .build();
        Role cashierRole = Role.builder()
                .name(Role.RoleName.CASHIER)
                .description("Cashier staff")
                .build();
        Role inventoryRole = Role.builder()
                .name(Role.RoleName.INVENTORY)
                .description("Inventory staff")
                .build();

        when(roleRepository.findByName(Role.RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName(Role.RoleName.MANAGER)).thenReturn(Optional.of(managerRole));
        when(roleRepository.findByName(Role.RoleName.BAKER)).thenReturn(Optional.of(bakerRole));
        when(roleRepository.findByName(Role.RoleName.CASHIER)).thenReturn(Optional.of(cashierRole));
        when(roleRepository.findByName(Role.RoleName.INVENTORY)).thenReturn(Optional.of(inventoryRole));
        when(userRepository.saveAll(any())).thenReturn(Arrays.asList());

        // When
        dataInitializer.createDevelopmentUsers();

        // Then
        verify(userRepository).count();
        verify(roleRepository, times(5)).findByName(any());
        verify(passwordEncoder, atLeast(7)).encode(any());
        verify(userRepository).saveAll(any());
    }

    @Test
    void testCreateDevelopmentUsersWhenAlreadyExist() {
        // Given
        when(userRepository.count()).thenReturn(5L);

        // When
        dataInitializer.createDevelopmentUsers();

        // Then
        verify(userRepository).count();
        verify(roleRepository, never()).findByName(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).saveAll(any());
    }

    @Test
    void testCreateProductionAdminWhenEmpty() {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("admin123")).thenReturn("encoded_admin_password");

        Role adminRole = Role.builder()
                .name(Role.RoleName.ADMIN)
                .description("Full system access")
                .build();

        when(roleRepository.findByName(Role.RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any())).thenReturn(new User());

        // When
        dataInitializer.createProductionAdmin();

        // Then
        verify(userRepository).count();
        verify(roleRepository).findByName(Role.RoleName.ADMIN);
        verify(passwordEncoder).encode("admin123");
        verify(userRepository).save(any());
    }

    @Test
    void testCreateProductionAdminWhenAlreadyExist() {
        // Given
        when(userRepository.count()).thenReturn(1L);

        // When
        dataInitializer.createProductionAdmin();

        // Then
        verify(userRepository).count();
        verify(roleRepository, never()).findByName(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateProductionAdminThrowsExceptionWhenRoleNotFound() {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName(Role.RoleName.ADMIN)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dataInitializer.createProductionAdmin();
        });

        verify(userRepository).count();
        verify(roleRepository).findByName(Role.RoleName.ADMIN);
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testPasswordEncoderIntegration() {
        // Test that password encoder is working
        String rawPassword = "test123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("wrongpassword", encodedPassword));
    }

    @Test
    void testRoleCreation() {
        // Test role creation logic
        Role adminRole = Role.builder()
                .name(Role.RoleName.ADMIN)
                .description(Role.RoleName.ADMIN.getDefaultDescription())
                .build();

        assertEquals(Role.RoleName.ADMIN, adminRole.getName());
        assertEquals("Full system access including user management", adminRole.getDescription());
    }

    @Test
    void testUserCreation() {
        // Test user creation logic
        Role adminRole = Role.builder()
                .name(Role.RoleName.ADMIN)
                .description("Test admin role")
                .build();

        User testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("test123"))
                .firstName("Test")
                .lastName("User")
                .employeeId("TEST001")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        testUser.addRole(adminRole);

        assertEquals("testuser", testUser.getUsername());
        assertEquals("test@example.com", testUser.getEmail());
        assertEquals("Test User", testUser.getFullName());
        assertEquals("TEST001", testUser.getEmployeeId());
        assertTrue(testUser.isEnabled());
        assertTrue(testUser.hasRole(Role.RoleName.ADMIN));
        assertNotNull(testUser.getPassword());
        assertNotEquals("test123", testUser.getPassword());
    }
}
