package com.pw.bakery.flow.repository;

import com.pw.bakery.flow.domain.model.Role;
import com.pw.bakery.flow.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations
 * Provides data access methods for user management
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by employee ID
     * @param employeeId the employee ID to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmployeeId(String employeeId);

    /**
     * Check if a user exists by username
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if a user exists by email
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if a user exists by employee ID
     * @param employeeId the employee ID to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmployeeId(String employeeId);

    /**
     * Find users by role
     * @param role the role to search for
     * @return List of users with the specified role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") Role role);

    /**
     * Find users by role name
     * @param roleName the role name to search for
     * @return List of users with the specified role name
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") Role.RoleName roleName);

    /**
     * Find users by multiple role names
     * @param roleNames the role names to search for
     * @return List of users with any of the specified roles
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames")
    List<User> findByRoleNameIn(@Param("roleNames") List<Role.RoleName> roleNames);

    /**
     * Find users by first name and last name (case insensitive)
     * @param firstName the first name to search for
     * @param lastName the last name to search for
     * @return List of matching users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) = LOWER(:firstName) AND LOWER(u.lastName) = LOWER(:lastName)")
    List<User> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(@Param("firstName") String firstName,
                                                             @Param("lastName") String lastName);

    /**
     * Find users by full name search (searches in first name or last name)
     * @param name the name to search for
     * @return List of matching users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find enabled users only
     * @return List of enabled users
     */
    List<User> findByEnabledTrue();

    /**
     * Find disabled users only
     * @return List of disabled users
     */
    List<User> findByEnabledFalse();

    /**
     * Find users created within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return List of users created in the date range
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Find users who haven't logged in since a specific date
     * @param lastLoginBefore the date threshold
     * @return List of inactive users
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin < :lastLoginBefore OR u.lastLogin IS NULL")
    List<User> findInactiveUsers(@Param("lastLoginBefore") LocalDateTime lastLoginBefore);

    /**
     * Find users by phone number
     * @param phoneNumber the phone number to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Count users by role
     * @param roleName the role name to count
     * @return number of users with the specified role
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") Role.RoleName roleName);

    /**
     * Get user statistics
     * @return Array containing total users, enabled users, and disabled users
     */
    @Query("SELECT COUNT(u), SUM(CASE WHEN u.enabled = true THEN 1 ELSE 0 END), SUM(CASE WHEN u.enabled = false THEN 1 ELSE 0 END) FROM User u")
    Object[] getUserStatistics();

    /**
     * Find users with multiple roles
     * @param minRoles minimum number of roles a user should have
     * @return List of users with multiple roles
     */
    @Query("SELECT u FROM User u JOIN u.roles r GROUP BY u HAVING COUNT(r) >= :minRoles")
    List<User> findUsersWithMultipleRoles(@Param("minRoles") int minRoles);

    /**
     * Search users by multiple criteria
     * @param username partial username match (optional)
     * @param email partial email match (optional)
     * @param firstName partial first name match (optional)
     * @param lastName partial last name match (optional)
     * @param employeeId partial employee ID match (optional)
     * @param roleName specific role name filter (optional)
     * @param enabled enabled status filter (optional)
     * @return List of matching users
     */
    @Query("SELECT u FROM User u LEFT JOIN u.roles r " +
           "WHERE (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
           "AND (:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) " +
           "AND (:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) " +
           "AND (:employeeId IS NULL OR LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :employeeId, '%'))) " +
           "AND (:roleName IS NULL OR r.name = :roleName) " +
           "AND (:enabled IS NULL OR u.enabled = :enabled)")
    List<User> searchUsers(@Param("username") String username,
                           @Param("email") String email,
                           @Param("firstName") String firstName,
                           @Param("lastName") String lastName,
                           @Param("employeeId") String employeeId,
                           @Param("roleName") Role.RoleName roleName,
                           @Param("enabled") Boolean enabled);

    /**
     * Find recently created users
     * @param since the date to start from
     * @return List of recently created users
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentlyCreatedUsers(@Param("since") LocalDateTime since);
}
