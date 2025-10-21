package com.pw.bakery.flow.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

/**
 * Role entity for user role management in bakery system
 * Defines different roles and their permissions
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleName name;

    @Column(length = 500)
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    /**
     * Available roles in the bakery system
     */
    public enum RoleName {
        ADMIN("Administrator", "Full system access including user management"),
        MANAGER("Manager", "Can manage operations, inventory, and view reports"),
        BAKER("Baker", "Can manage production workflows and view assigned tasks"),
        CASHIER("Cashier", "Can handle orders and payments"),
        INVENTORY("Inventory Staff", "Can manage stock and inventory");

        private final String displayName;
        private final String defaultDescription;

        RoleName(String displayName, String defaultDescription) {
            this.displayName = displayName;
            this.defaultDescription = defaultDescription;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDefaultDescription() {
            return defaultDescription;
        }
    }
}
