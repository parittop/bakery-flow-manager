package com.pw.bakery.flow.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.pw.bakery.flow.domain.model.Role;
import com.pw.bakery.flow.util.UserTestData;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test class for RoleRepository
 * Tests all repository methods and database operations
 */
@DataJpaTest
@ActiveProfiles("test")
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    private Role adminRole;
    private Role managerRole;
    private Role bakerRole;

    @BeforeEach
    void setUp() {
        // Create and persist roles
        adminRole = UserTestData.createAdminRole();
        managerRole = UserTestData.createManagerRole();
        bakerRole = UserTestData.createBakerRole();

        entityManager.persist(adminRole);
        entityManager.persist(managerRole);
        entityManager.persist(bakerRole);
        entityManager.flush();
    }

    @Test
    void testFindByName() {
        Optional<Role> found = roleRepository.findByName(Role.RoleName.ADMIN);
        assertTrue(found.isPresent());
        assertEquals(Role.RoleName.ADMIN, found.get().getName());

        Optional<Role> notFound = roleRepository.findByName(
            Role.RoleName.INVENTORY
        );
        assertFalse(notFound.isPresent());
    }

    @Test
    void testExistsByName() {
        assertTrue(roleRepository.existsByName(Role.RoleName.ADMIN));
        assertTrue(roleRepository.existsByName(Role.RoleName.MANAGER));
        assertTrue(roleRepository.existsByName(Role.RoleName.BAKER));
        assertFalse(roleRepository.existsByName(Role.RoleName.INVENTORY));
    }

    @Test
    void testFindByNameIgnoreCase() {
        // Since enum comparison is case insensitive, this should work
        Optional<Role> found = roleRepository.findByNameIgnoreCase(
            Role.RoleName.ADMIN
        );
        assertTrue(found.isPresent());
        assertEquals(Role.RoleName.ADMIN, found.get().getName());
    }

    @Test
    void testFindRoleUserCounts() {
        // Initially no users, should return empty or zero counts
        Object[] roleCounts = roleRepository.findRoleUserCounts();
        assertNotNull(roleCounts);
        // Since we haven't added users, counts should be 0
    }

    @Test
    void testFindByDescriptionContainingIgnoreCase() {
        // Test with existing descriptions
        Iterable<Role> roles =
            roleRepository.findByDescriptionContainingIgnoreCase(
                "Full system access"
            );
        assertTrue(roles.iterator().hasNext());

        Iterable<Role> allRoles =
            roleRepository.findByDescriptionContainingIgnoreCase("operations");
        assertTrue(allRoles.iterator().hasNext());

        // Test with non-existing description
        Iterable<Role> noRoles =
            roleRepository.findByDescriptionContainingIgnoreCase("nonexistent");
        assertFalse(noRoles.iterator().hasNext());
    }

    @Test
    void testSaveRole() {
        Role inventoryRole = UserTestData.createInventoryRole();
        Role savedRole = roleRepository.save(inventoryRole);

        assertNotNull(savedRole.getId());
        assertEquals(Role.RoleName.INVENTORY, savedRole.getName());
        assertEquals(
            Role.RoleName.INVENTORY.getDefaultDescription(),
            savedRole.getDescription()
        );
    }

    @Test
    void testUpdateRole() {
        adminRole.setDescription("Updated administrator description");
        Role updatedRole = roleRepository.save(adminRole);

        assertEquals(
            "Updated administrator description",
            updatedRole.getDescription()
        );
        assertEquals(adminRole.getId(), updatedRole.getId());
    }

    @Test
    void testDeleteRole() {
        Long roleId = bakerRole.getId();
        roleRepository.delete(bakerRole);
        entityManager.flush();

        Optional<Role> deleted = roleRepository.findById(roleId);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testFindAllRoles() {
        List<Role> allRoles = roleRepository.findAll();
        assertEquals(3, allRoles.size());

        assertTrue(
            allRoles.stream().anyMatch(r -> r.getName() == Role.RoleName.ADMIN)
        );
        assertTrue(
            allRoles
                .stream()
                .anyMatch(r -> r.getName() == Role.RoleName.MANAGER)
        );
        assertTrue(
            allRoles.stream().anyMatch(r -> r.getName() == Role.RoleName.BAKER)
        );
    }

    @Test
    void testFindById() {
        Optional<Role> found = roleRepository.findById(adminRole.getId());
        assertTrue(found.isPresent());
        assertEquals(Role.RoleName.ADMIN, found.get().getName());

        Optional<Role> notFound = roleRepository.findById(999L);
        assertFalse(notFound.isPresent());
    }

    @Test
    void testCount() {
        long count = roleRepository.count();
        assertEquals(3, count);
    }

    @Test
    void testSaveAll() {
        Role cashierRole = UserTestData.createCashierRole();
        Role inventoryRole = UserTestData.createInventoryRole();

        List<Role> savedRoles = roleRepository.saveAll(
            Arrays.asList(cashierRole, inventoryRole)
        );
        assertEquals(2, savedRoles.size());

        long totalRoles = roleRepository.count();
        assertEquals(5, totalRoles);
    }

    @Test
    void testDeleteById() {
        Long roleId = managerRole.getId();
        roleRepository.deleteById(roleId);
        entityManager.flush();

        Optional<Role> deleted = roleRepository.findById(roleId);
        assertFalse(deleted.isPresent());

        long remainingRoles = roleRepository.count();
        assertEquals(2, remainingRoles);
    }

    @Test
    void testExistsById() {
        assertTrue(roleRepository.existsById(adminRole.getId()));
        assertTrue(roleRepository.existsById(managerRole.getId()));
        assertTrue(roleRepository.existsById(bakerRole.getId()));
        assertFalse(roleRepository.existsById(999L));
    }

    @Test
    void testRoleWithUsers() {
        // This test would require setting up users with roles
        // For now, we test the relationship setup
        assertTrue(adminRole.getUsers().isEmpty());
    }

    @Test
    void testRoleEnumProperties() {
        Role testRole = Role.builder()
            .name(Role.RoleName.CASHIER)
            .description(Role.RoleName.CASHIER.getDefaultDescription())
            .build();

        assertEquals(Role.RoleName.CASHIER, testRole.getName());
        assertEquals("Cashier", Role.RoleName.CASHIER.getDisplayName());
        assertEquals(
            "Can handle orders and payments",
            Role.RoleName.CASHIER.getDefaultDescription()
        );
    }

    @Test
    void testUniqueRoleName() {
        // Test that we cannot save duplicate role names
        Role duplicateAdmin = Role.builder()
            .name(Role.RoleName.ADMIN)
            .description("Duplicate admin role")
            .build();

        // This should throw an exception due to unique constraint
        assertThrows(Exception.class, () -> {
            entityManager.persist(duplicateAdmin);
            entityManager.flush();
        });
    }

    @Test
    void testRoleDescriptionSearch() {
        // Test partial match in description
        Iterable<Role> systemRoles =
            roleRepository.findByDescriptionContainingIgnoreCase("system");
        Iterable<Role> fullAccessRoles =
            roleRepository.findByDescriptionContainingIgnoreCase("full");

        // Should find admin role with "Full system access"
        assertTrue(fullAccessRoles.iterator().hasNext());
    }
}
