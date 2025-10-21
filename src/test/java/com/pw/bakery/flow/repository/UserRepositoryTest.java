package com.pw.bakery.flow.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.pw.bakery.flow.domain.model.Role;
import com.pw.bakery.flow.domain.model.User;
import com.pw.bakery.flow.util.UserTestData;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test class for UserRepository
 * Tests all repository methods and database operations
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User adminUser;
    private User managerUser;
    private User bakerUser;
    private Role adminRole;
    private Role managerRole;
    private Role bakerRole;

    @BeforeEach
    void setUp() {
        // Create and persist roles
        adminRole = UserTestData.createAdminRole();
        managerRole = UserTestData.createManagerRole();
        bakerRole = UserTestData.createBakerRole();

        adminRole = roleRepository.save(adminRole);
        managerRole = roleRepository.save(managerRole);
        bakerRole = roleRepository.save(bakerRole);

        // Create and persist users
        adminUser = UserTestData.createAdminUser();
        managerUser = UserTestData.createManagerUser();
        bakerUser = UserTestData.createBakerUser();

        adminUser = userRepository.save(adminUser);
        managerUser = userRepository.save(managerUser);
        bakerUser = userRepository.save(bakerUser);
    }

    @Test
    void testFindByUsername() {
        Optional<User> found = userRepository.findByUsername("admin");
        assertTrue(found.isPresent());
        assertEquals("admin", found.get().getUsername());

        Optional<User> notFound = userRepository.findByUsername("nonexistent");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByEmail() {
        Optional<User> found = userRepository.findByEmail("admin@bakery.com");
        assertTrue(found.isPresent());
        assertEquals("admin@bakery.com", found.get().getEmail());

        Optional<User> notFound = userRepository.findByEmail(
            "nonexistent@test.com"
        );
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByEmployeeId() {
        Optional<User> found = userRepository.findByEmployeeId("EMP001");
        assertTrue(found.isPresent());
        assertEquals("EMP001", found.get().getEmployeeId());

        Optional<User> notFound = userRepository.findByEmployeeId("EMP999");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testExistsByUsername() {
        assertTrue(userRepository.existsByUsername("admin"));
        assertTrue(userRepository.existsByUsername("manager"));
        assertTrue(userRepository.existsByUsername("baker"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testExistsByEmail() {
        assertTrue(userRepository.existsByEmail("admin@bakery.com"));
        assertTrue(userRepository.existsByEmail("manager@bakery.com"));
        assertTrue(userRepository.existsByEmail("baker@bakery.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@test.com"));
    }

    @Test
    void testExistsByEmployeeId() {
        assertTrue(userRepository.existsByEmployeeId("EMP001"));
        assertTrue(userRepository.existsByEmployeeId("EMP002"));
        assertTrue(userRepository.existsByEmployeeId("EMP003"));
        assertFalse(userRepository.existsByEmployeeId("EMP999"));
    }

    @Test
    void testFindByRole() {
        List<User> adminUsers = userRepository.findByRole(adminRole);
        assertEquals(1, adminUsers.size());
        assertEquals("admin", adminUsers.get(0).getUsername());

        List<User> bakerUsers = userRepository.findByRole(bakerRole);
        assertEquals(1, bakerUsers.size());
        assertEquals("baker", bakerUsers.get(0).getUsername());
    }

    @Test
    void testFindByRoleName() {
        List<User> adminUsers = userRepository.findByRoleName(
            Role.RoleName.ADMIN
        );
        assertEquals(1, adminUsers.size());
        assertEquals("admin", adminUsers.get(0).getUsername());

        List<User> managerUsers = userRepository.findByRoleName(
            Role.RoleName.MANAGER
        );
        assertEquals(1, managerUsers.size());
        assertEquals("manager", managerUsers.get(0).getUsername());
    }

    @Test
    void testFindByRoleNameIn() {
        List<Role.RoleName> roleNames = Arrays.asList(
            Role.RoleName.ADMIN,
            Role.RoleName.MANAGER
        );
        List<User> users = userRepository.findByRoleNameIn(roleNames);
        assertEquals(2, users.size());
        assertTrue(
            users.stream().anyMatch(u -> u.getUsername().equals("admin"))
        );
        assertTrue(
            users.stream().anyMatch(u -> u.getUsername().equals("manager"))
        );
    }

    @Test
    void testFindByFirstNameIgnoreCaseAndLastNameIgnoreCase() {
        List<User> users =
            userRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(
                "ADMIN",
                "USER"
            );
        assertEquals(1, users.size());
        assertEquals("admin", users.get(0).getUsername());

        List<User> lowerCase =
            userRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(
                "admin",
                "user"
            );
        assertEquals(1, lowerCase.size());
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        List<User> users = userRepository.findByNameContainingIgnoreCase(
            "Admin"
        );
        assertEquals(1, users.size());
        assertEquals("admin", users.get(0).getUsername());

        List<User> partial = userRepository.findByNameContainingIgnoreCase(
            "Manage"
        );
        assertEquals(1, partial.size());
        assertEquals("manager", partial.get(0).getUsername());
    }

    @Test
    void testFindByEnabledTrue() {
        List<User> enabledUsers = userRepository.findByEnabledTrue();
        assertEquals(3, enabledUsers.size());
        assertTrue(enabledUsers.stream().allMatch(User::isEnabled));
    }

    @Test
    void testFindByEnabledFalse() {
        // Disable a user
        adminUser.setEnabled(false);
        userRepository.save(adminUser);

        List<User> disabledUsers = userRepository.findByEnabledFalse();
        assertEquals(1, disabledUsers.size());
        assertEquals("admin", disabledUsers.get(0).getUsername());
    }

    @Test
    void testFindByCreatedAtBetween() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);

        List<User> users = userRepository.findByCreatedAtBetween(
            yesterday,
            tomorrow
        );
        assertEquals(3, users.size());

        List<User> empty = userRepository.findByCreatedAtBetween(
            tomorrow,
            tomorrow.plusDays(1)
        );
        assertTrue(empty.isEmpty());
    }

    @Test
    void testFindInactiveUsers() {
        LocalDateTime threshold = LocalDateTime.now().plusMinutes(1);
        List<User> inactive = userRepository.findInactiveUsers(threshold);
        assertEquals(3, inactive.size()); // All users have null lastLogin
    }

    @Test
    void testFindByPhoneNumber() {
        Optional<User> found = userRepository.findByPhoneNumber("0812345678");
        assertTrue(found.isPresent());
        assertEquals("admin", found.get().getUsername());

        Optional<User> notFound = userRepository.findByPhoneNumber(
            "9999999999"
        );
        assertFalse(notFound.isPresent());
    }

    @Test
    void testCountByRoleName() {
        long adminCount = userRepository.countByRoleName(Role.RoleName.ADMIN);
        assertEquals(1, adminCount);

        long bakerCount = userRepository.countByRoleName(Role.RoleName.BAKER);
        assertEquals(1, bakerCount);

        long inventoryCount = userRepository.countByRoleName(
            Role.RoleName.INVENTORY
        );
        assertEquals(0, inventoryCount);
    }

    @Test
    void testGetUserStatistics() {
        Object[] stats = userRepository.getUserStatistics();
        assertNotNull(stats);
        assertEquals(3, stats.length);
        assertEquals(3L, stats[0]); // Total users
        assertEquals(3L, stats[1]); // Enabled users
        assertEquals(0L, stats[2]); // Disabled users
    }

    @Test
    void testFindUsersWithMultipleRoles() {
        // Add a second role to admin user
        adminUser.addRole(managerRole);
        userRepository.save(adminUser);

        List<User> multiRoleUsers = userRepository.findUsersWithMultipleRoles(
            2
        );
        assertEquals(1, multiRoleUsers.size());
        assertEquals("admin", multiRoleUsers.get(0).getUsername());

        List<User> singleRoleUsers = userRepository.findUsersWithMultipleRoles(
            1
        );
        assertEquals(3, singleRoleUsers.size());
    }

    @Test
    void testSearchUsers() {
        // Search by username
        List<User> results = userRepository.searchUsers(
            "admin",
            null,
            null,
            null,
            null,
            null,
            null
        );
        assertEquals(1, results.size());
        assertEquals("admin", results.get(0).getUsername());

        // Search by email
        results = userRepository.searchUsers(
            null,
            "manager@bakery.com",
            null,
            null,
            null,
            null,
            null
        );
        assertEquals(1, results.size());
        assertEquals("manager", results.get(0).getUsername());

        // Search by role
        results = userRepository.searchUsers(
            null,
            null,
            null,
            null,
            null,
            Role.RoleName.BAKER,
            null
        );
        assertEquals(1, results.size());
        assertEquals("baker", results.get(0).getUsername());

        // Search by enabled status
        results = userRepository.searchUsers(
            null,
            null,
            null,
            null,
            null,
            null,
            true
        );
        assertEquals(3, results.size());

        // Search with multiple criteria
        results = userRepository.searchUsers(
            "admin",
            null,
            "Admin",
            null,
            null,
            Role.RoleName.ADMIN,
            true
        );
        assertEquals(1, results.size());
    }

    @Test
    void testFindRecentlyCreatedUsers() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(1);
        List<User> recentUsers = userRepository.findRecentlyCreatedUsers(since);
        assertEquals(3, recentUsers.size());

        LocalDateTime future = LocalDateTime.now().plusMinutes(1);
        List<User> futureUsers = userRepository.findRecentlyCreatedUsers(
            future
        );
        assertTrue(futureUsers.isEmpty());
    }

    @Test
    void testSaveUser() {
        User newUser = UserTestData.createCustomUser(
            "newuser",
            "newuser@bakery.com",
            "New",
            "User",
            "EMP999",
            new HashSet<>(Arrays.asList(adminRole))
        );
        User savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getId());
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("newuser@bakery.com", savedUser.getEmail());
        assertTrue(savedUser.hasRole(Role.RoleName.ADMIN));
    }

    @Test
    void testUpdateUser() {
        adminUser.setPhoneNumber("0999999999");
        User updatedUser = userRepository.save(adminUser);

        assertEquals("0999999999", updatedUser.getPhoneNumber());
        assertEquals(adminUser.getId(), updatedUser.getId());
    }

    @Test
    void testDeleteUser() {
        Long userId = bakerUser.getId();
        userRepository.delete(bakerUser);
        entityManager.flush();

        Optional<User> deleted = userRepository.findById(userId);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testFindAllUsers() {
        List<User> allUsers = userRepository.findAll();
        assertEquals(3, allUsers.size());

        assertTrue(
            allUsers.stream().anyMatch(u -> u.getUsername().equals("admin"))
        );
        assertTrue(
            allUsers.stream().anyMatch(u -> u.getUsername().equals("manager"))
        );
        assertTrue(
            allUsers.stream().anyMatch(u -> u.getUsername().equals("baker"))
        );
    }

    @Test
    void testUserTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        User newUser = UserTestData.createCustomUser(
            "timestamp",
            "time@bakery.com",
            "Time",
            "Test",
            "EMP888",
            new HashSet<>(Arrays.asList(adminRole))
        );
        User savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
        assertTrue(savedUser.getCreatedAt().isAfter(now.minusMinutes(1)));
        assertTrue(savedUser.getUpdatedAt().isAfter(now.minusMinutes(1)));
    }

    @Test
    void testUserWithRoles() {
        User user = userRepository.findByUsername("admin").orElseThrow();
        assertEquals(1, user.getRoles().size());
        assertTrue(user.hasRole(Role.RoleName.ADMIN));

        // Test role methods
        assertTrue(user.hasRole(Role.RoleName.ADMIN));
        assertFalse(user.hasRole(Role.RoleName.BAKER));
        assertTrue(user.hasAnyRole(Role.RoleName.ADMIN, Role.RoleName.MANAGER));
        assertFalse(
            user.hasAnyRole(Role.RoleName.BAKER, Role.RoleName.CASHIER)
        );
    }

    @Test
    void testUniqueConstraints() {
        // Test that we cannot save duplicate usernames
        User duplicateUser = User.builder()
            .username("admin")
            .email("duplicate@bakery.com")
            .password("password")
            .firstName("Duplicate")
            .lastName("User")
            .employeeId("EMP999")
            .roles(new HashSet<>(Arrays.asList(bakerRole)))
            .build();

        assertThrows(Exception.class, () -> {
            userRepository.save(duplicateUser);
            entityManager.flush();
        });
    }
}
