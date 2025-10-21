package com.pw.bakery.flow.domain.model;

import com.pw.bakery.flow.util.UserTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for User and Role entities
 * Validates entity relationships, business logic, and constraints
 */
@SpringBootTest
@ActiveProfiles("test")
class UserRoleTest {

    private User adminUser;
    private User bakerUser;
    private Role adminRole;
    private Role bakerRole;

    @BeforeEach
    void setUp() {
        adminRole = UserTestData.createAdminRole();
        bakerRole = UserTestData.createBakerRole();

        adminUser = UserTestData.createAdminUser();
        bakerUser = UserTestData.createBakerUser();
    }

    @Test
    void testRoleCreation() {
        assertNotNull(adminRole);
        assertEquals(Role.RoleName.ADMIN, adminRole.getName());
        assertNotNull(adminRole.getDescription());
        assertTrue(adminRole.getUsers().isEmpty());
    }

    @Test
    void testRoleEnumValues() {
        assertEquals(5, Role.RoleName.values().length);

        Role.RoleName[] roles = Role.RoleName.values();
        assertTrue(Set.of(roles).contains(Role.RoleName.ADMIN));
        assertTrue(Set.of(roles).contains(Role.RoleName.MANAGER));
        assertTrue(Set.of(roles).contains(Role.RoleName.BAKER));
        assertTrue(Set.of(roles).contains(Role.RoleName.CASHIER));
        assertTrue(Set.of(roles).contains(Role.RoleName.INVENTORY));
    }

    @Test
    void testRoleEnumDisplayNames() {
        assertEquals("Administrator", Role.RoleName.ADMIN.getDisplayName());
        assertEquals("Manager", Role.RoleName.MANAGER.getDisplayName());
        assertEquals("Baker", Role.RoleName.BAKER.getDisplayName());
        assertEquals("Cashier", Role.RoleName.CASHIER.getDisplayName());
        assertEquals("Inventory Staff", Role.RoleName.INVENTORY.getDisplayName());
    }

    @Test
    void testUserCreation() {
        assertNotNull(adminUser);
        assertEquals("admin", adminUser.getUsername());
        assertEquals("admin@bakery.com", adminUser.getEmail());
        assertEquals("Admin User", adminUser.getFullName());
        assertEquals("EMP001", adminUser.getEmployeeId());
        assertTrue(adminUser.isEnabled());
        assertTrue(adminUser.hasRole(Role.RoleName.ADMIN));
    }

    @Test
    void testUserDetailsImplementation() {
        assertTrue(adminUser.isAccountNonExpired());
        assertTrue(adminUser.isAccountNonLocked());
        assertTrue(adminUser.isCredentialsNonExpired());
        assertTrue(adminUser.isEnabled());

        var authorities = adminUser.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testUserHasRoleMethod() {
        assertTrue(adminUser.hasRole(Role.RoleName.ADMIN));
        assertFalse(adminUser.hasRole(Role.RoleName.BAKER));
        assertFalse(bakerUser.hasRole(Role.RoleName.ADMIN));
        assertTrue(bakerUser.hasRole(Role.RoleName.BAKER));
    }

    @Test
    void testUserHasAnyRoleMethod() {
        assertTrue(adminUser.hasAnyRole(Role.RoleName.ADMIN));
        assertTrue(adminUser.hasAnyRole(Role.RoleName.ADMIN, Role.RoleName.MANAGER));
        assertFalse(adminUser.hasAnyRole(Role.RoleName.BAKER, Role.RoleName.CASHIER));

        // Test with multiple roles
        User multiRoleUser = UserTestData.createCustomUser(
                "multi", "multi@bakery.com", "Multi", "Role", "EMP999",
                new HashSet<>(Set.of(adminRole, bakerRole))
        );
        assertTrue(multiRoleUser.hasAnyRole(Role.RoleName.ADMIN, Role.RoleName.BAKER));
        assertTrue(multiRoleUser.hasAnyRole(Role.RoleName.MANAGER, Role.RoleName.ADMIN));
        assertFalse(multiRoleUser.hasAnyRole(Role.RoleName.CASHIER, Role.RoleName.INVENTORY));
    }

    @Test
    void testUserAddRemoveRole() {
        User user = UserTestData.createCustomUser(
                "test", "test@bakery.com", "Test", "User", "EMP999",
                new HashSet<>()
        );

        assertTrue(user.getRoles().isEmpty());

        user.addRole(adminRole);
        assertEquals(1, user.getRoles().size());
        assertTrue(user.hasRole(Role.RoleName.ADMIN));
        assertTrue(adminRole.getUsers().contains(user));

        user.removeRole(adminRole);
        assertTrue(user.getRoles().isEmpty());
        assertFalse(user.hasRole(Role.RoleName.ADMIN));
        assertFalse(adminRole.getUsers().contains(user));
    }

    @Test
    void testUserWithMultipleRoles() {
        Set<Role> multipleRoles = new HashSet<>();
        multipleRoles.add(adminRole);
        multipleRoles.add(bakerRole);

        User user = UserTestData.createCustomUser(
                "multi", "multi@bakery.com", "Multi", "Role", "EMP999",
                multipleRoles
        );

        assertEquals(2, user.getRoles().size());
        assertTrue(user.hasRole(Role.RoleName.ADMIN));
        assertTrue(user.hasRole(Role.RoleName.BAKER));

        var authorities = user.getAuthorities();
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_BAKER")));
    }

    @Test
    void testUserValidation() {
        assertTrue(UserTestData.isValidUser(adminUser));
        assertTrue(UserTestData.isValidUser(bakerUser));

        User invalidUser = new User();
        assertFalse(UserTestData.isValidUser(invalidUser));

        invalidUser.setUsername("test");
        assertFalse(UserTestData.isValidUser(invalidUser));

        invalidUser.setEmail("test@test.com");
        invalidUser.setPassword("password");
        invalidUser.setFirstName("Test");
        invalidUser.setLastName("User");
        invalidUser.setRoles(new HashSet<>(Set.of(adminRole)));
        assertTrue(UserTestData.isValidUser(invalidUser));
    }

    @Test
    void testUserDisplayInfo() {
        String displayInfo = UserTestData.getUserDisplayInfo(adminUser);
        assertNotNull(displayInfo);
        assertTrue(displayInfo.contains("Admin User"));
        assertTrue(displayInfo.contains("(admin)"));
        assertTrue(displayInfo.contains("EMP001"));
        assertTrue(displayInfo.contains("[Administrator]"));

        String nullUserDisplay = UserTestData.getUserDisplayInfo(null);
        assertEquals("Unknown User", nullUserDisplay);
    }

    @Test
    void testCreateAllUsers() {
        Set<User> allUsers = UserTestData.createAllUsers();
        assertEquals(4, allUsers.size());

        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("admin")));
        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("manager")));
        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("baker")));
        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("cashier")));
    }

    @Test
    void testCreateAllRoles() {
        Set<Role> allRoles = UserTestData.createAllRoles();
        assertEquals(5, allRoles.size());

        assertTrue(allRoles.stream().anyMatch(r -> r.getName() == Role.RoleName.ADMIN));
        assertTrue(allRoles.stream().anyMatch(r -> r.getName() == Role.RoleName.MANAGER));
        assertTrue(allRoles.stream().anyMatch(r -> r.getName() == Role.RoleName.BAKER));
        assertTrue(allRoles.stream().anyMatch(r -> r.getName() == Role.RoleName.CASHIER));
        assertTrue(allRoles.stream().anyMatch(r -> r.getName() == Role.RoleName.INVENTORY));
    }

    @Test
    void testUserTimestamps() {
        User user = UserTestData.createCustomUser(
                "timestamp", "time@bakery.com", "Time", "Test", "EMP888",
                new HashSet<>(Set.of(adminRole))
        );

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(user.getCreatedAt()) ||
                  user.getUpdatedAt().equals(user.getCreatedAt()));
    }

    @Test
    void testUserAccountStatus() {
        User user = adminUser;

        // Test default values
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());

        // Test disabling user
        user.setEnabled(false);
        assertFalse(user.isEnabled());
        assertFalse(user.isAccountNonExpired()); // In Spring Security, disabled accounts are considered expired
    }
}
