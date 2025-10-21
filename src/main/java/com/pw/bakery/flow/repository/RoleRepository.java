package com.pw.bakery.flow.repository;

import com.pw.bakery.flow.domain.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Role entity operations
 * Provides data access methods for role management
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Find role by name
     * @param roleName the role name to search for
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(Role.RoleName roleName);

    /**
     * Check if a role exists by name
     * @param roleName the role name to check
     * @return true if role exists, false otherwise
     */
    boolean existsByName(Role.RoleName roleName);

    /**
     * Find role by name ignoring case
     * @param roleName the role name to search for (case insensitive)
     * @return Optional containing the role if found
     */
    @Query("SELECT r FROM Role r WHERE r.name = :roleName")
    Optional<Role> findByNameIgnoreCase(
        @Param("roleName") Role.RoleName roleName
    );

    /**
     * Count users in each role
     * @return List of role statistics with user counts
     */
    @Query(
        "SELECT r.name, COUNT(u) FROM Role r LEFT JOIN r.users u GROUP BY r.name ORDER BY COUNT(u) DESC"
    )
    Object[] findRoleUserCounts();

    /**
     * Find roles with specific description containing text
     * @param descriptionText the text to search in descriptions
     * @return Iterable of matching roles
     */
    @Query(
        "SELECT r FROM Role r WHERE LOWER(r.description) LIKE LOWER(CONCAT('%', :descriptionText, '%'))"
    )
    Iterable<Role> findByDescriptionContainingIgnoreCase(
        @Param("descriptionText") String descriptionText
    );
}
