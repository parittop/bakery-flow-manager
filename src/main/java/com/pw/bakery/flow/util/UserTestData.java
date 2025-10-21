package com.pw.bakery.flow.util;

import com.pw.bakery.flow.domain.model.Role;
import com.pw.bakery.flow.domain.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for creating test data for User and Role entities
 * Useful for development, testing, and data seeding
 */
public class UserTestData {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Create a default admin user
     */
    public static User createAdminUser() {
        return User.builder()
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
                .roles(new HashSet<>(Arrays.asList(createAdminRole())))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a default manager user
     */
    public static User createManagerUser() {
        return User.builder()
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
                .roles(new HashSet<>(Arrays.asList(createManagerRole())))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a default baker user
     */
    public static User createBakerUser() {
        return User.builder()
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
                .roles(new HashSet<>(Arrays.asList(createBakerRole())))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a default cashier user
     */
    public static User createCashierUser() {
        return User.builder()
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
                .roles(new HashSet<>(Arrays.asList(createCashierRole())))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create all default roles
     */
    public static Set<Role> createAllRoles() {
        return new HashSet<>(Arrays.asList(
                createAdminRole(),
                createManagerRole(),
                createBakerRole(),
                createCashierRole(),
                createInventoryRole()
        ));
    }

    /**
     * Create all default users
     */
    public static Set<User> createAllUsers() {
        Set<Role> allRoles = createAllRoles();
        return new HashSet<>(Arrays.asList(
                createAdminUser(),
                createManagerUser(),
                createBakerUser(),
                createCashierUser()
        ));
    }

    /**
     * Create a custom user with specified roles
     */
    public static User createCustomUser(String username, String email, String firstName, String lastName,
                                       String employeeId, Set<Role> roles) {
        return User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber("08" + String.format("%08d", (int)(Math.random() * 100000000)))
                .employeeId(employeeId)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Role creation methods

    public static Role createAdminRole() {
        return Role.builder()
                .name(Role.RoleName.ADMIN)
                .description(Role.RoleName.ADMIN.getDefaultDescription())
                .users(new HashSet<>())
                .build();
    }

    public static Role createManagerRole() {
        return Role.builder()
                .name(Role.RoleName.MANAGER)
                .description(Role.RoleName.MANAGER.getDefaultDescription())
                .users(new HashSet<>())
                .build();
    }

    public static Role createBakerRole() {
        return Role.builder()
                .name(Role.RoleName.BAKER)
                .description(Role.RoleName.BAKER.getDefaultDescription())
                .users(new HashSet<>())
                .build();
    }

    public static Role createCashierRole() {
        return Role.builder()
                .name(Role.RoleName.CASHIER)
                .description(Role.RoleName.CASHIER.getDefaultDescription())
                .users(new HashSet<>())
                .build();
    }

    public static Role createInventoryRole() {
        return Role.builder()
                .name(Role.RoleName.INVENTORY)
                .description(Role.RoleName.INVENTORY.getDefaultDescription())
                .users(new HashSet<>())
                .build();
    }

    /**
     * Validate user data
     */
    public static boolean isValidUser(User user) {
        return user != null &&
               user.getUsername() != null && !user.getUsername().trim().isEmpty() &&
               user.getEmail() != null && !user.getEmail().trim().isEmpty() &&
               user.getPassword() != null && !user.getPassword().trim().isEmpty() &&
               user.getFirstName() != null && !user.getFirstName().trim().isEmpty() &&
               user.getLastName() != null && !user.getLastName().trim().isEmpty() &&
               user.getRoles() != null && !user.getRoles().isEmpty();
    }

    /**
     * Get user display information
     */
    public static String getUserDisplayInfo(User user) {
        if (user == null) return "Unknown User";

        StringBuilder info = new StringBuilder();
        info.append(user.getFullName())
            .append(" (").append(user.getUsername()).append(")")
            .append(" - ").append(user.getEmployeeId());

        if (!user.getRoles().isEmpty()) {
            info.append(" [");
            user.getRoles().forEach(role -> info.append(role.getName().getDisplayName()).append(", "));
            info.setLength(info.length() - 2); // Remove last comma and space
            info.append("]");
        }

        return info.toString();
    }
}
