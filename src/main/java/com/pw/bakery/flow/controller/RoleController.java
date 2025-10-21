package com.pw.bakery.flow.controller;

import com.pw.bakery.flow.dto.ApiResponse;
import com.pw.bakery.flow.dto.RoleResponse;
import com.pw.bakery.flow.domain.model.Role;
import com.pw.bakery.flow.repository.RoleRepository;
import com.pw.bakery.flow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for Role Management
 * Provides CRUD operations for role management
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoleController {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    /**
     * Get all roles with pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "false") boolean includeUsers) {

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<Role> roles = roleRepository.findAll(pageable);
            List<RoleResponse> roleResponses;

            if (includeUsers) {
                roleResponses = roles.getContent().stream()
                        .map(RoleResponse::fromWithUsers)
                        .collect(Collectors.toList());
            } else {
                roleResponses = roles.getContent().stream()
                        .map(RoleResponse::from)
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(ApiResponse.success(roleResponses,
                String.format("Retrieved %d roles", roles.getTotalElements())));

        } catch (Exception e) {
            log.error("Error retrieving roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve roles"));
        }
    }

    /**
     * Get role by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeUsers) {

        try {
            Optional<Role> roleOpt = roleRepository.findById(id);
            if (roleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notFound("Role not found"));
            }

            Role role = roleOpt.get();
            RoleResponse roleResponse = includeUsers ?
                RoleResponse.fromWithUsers(role) : RoleResponse.from(role);

            return ResponseEntity.ok(ApiResponse.success(roleResponse, "Role retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving role {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve role"));
        }
    }

    /**
     * Get role by name
     */
    @GetMapping("/name/{roleName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByName(
            @PathVariable String roleName,
            @RequestParam(defaultValue = "false") boolean includeUsers) {

        try {
            Role.RoleName roleEnum;
            try {
                roleEnum = Role.RoleName.valueOf(roleName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.validationError("Invalid role name: " + roleName));
            }

            Optional<Role> roleOpt = roleRepository.findByName(roleEnum);
            if (roleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notFound("Role not found"));
            }

            Role role = roleOpt.get();
            RoleResponse roleResponse = includeUsers ?
                RoleResponse.fromWithUsers(role) : RoleResponse.from(role);

            return ResponseEntity.ok(ApiResponse.success(roleResponse, "Role retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving role by name {}: {}", roleName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve role"));
        }
    }

    /**
     * Get role statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Object[]>> getRoleStatistics() {
        try {
            Object[] roleStats = roleRepository.findRoleUserCounts();
            return ResponseEntity.ok(ApiResponse.success(roleStats, "Role statistics retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving role statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve role statistics"));
        }
    }

    /**
     * Search roles by description
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> searchRoles(
            @RequestParam String description,
            @RequestParam(defaultValue = "false") boolean includeUsers) {

        try {
            Iterable<Role> roles = roleRepository.findByDescriptionContainingIgnoreCase(description);
            List<RoleResponse> roleResponses;

            if (includeUsers) {
                roleResponses = ((java.util.Collection<Role>) roles).stream()
                        .map(RoleResponse::fromWithUsers)
                        .collect(Collectors.toList());
            } else {
                roleResponses = ((java.util.Collection<Role>) roles).stream()
                        .map(RoleResponse::from)
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(ApiResponse.success(roleResponses,
                String.format("Found %d roles matching description", roleResponses.size())));

        } catch (Exception e) {
            log.error("Error searching roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search roles"));
        }
    }

    /**
     * Create new role (limited to predefined roles)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @RequestParam Role.RoleName roleName,
            @RequestParam(required = false) String description) {

        try {
            // Check if role already exists
            if (roleRepository.existsByName(roleName)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.conflict("Role already exists"));
            }

            // Create role with default description if not provided
            Role role = Role.builder()
                    .name(roleName)
                    .description(description != null ? description : roleName.getDefaultDescription())
                    .users(new java.util.HashSet<>())
                    .build();

            Role savedRole = roleRepository.save(role);
            RoleResponse roleResponse = RoleResponse.from(savedRole);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(roleResponse, "Role created successfully", HttpStatus.CREATED));

        } catch (Exception e) {
            log.error("Error creating role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create role"));
        }
    }

    /**
     * Update role description
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @RequestParam String description) {

        try {
            Optional<Role> roleOpt = roleRepository.findById(id);
            if (roleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notFound("Role not found"));
            }

            Role role = roleOpt.get();
            role.setDescription(description);
            Role updatedRole = roleRepository.save(role);
            RoleResponse roleResponse = RoleResponse.from(updatedRole);

            return ResponseEntity.ok(ApiResponse.success(roleResponse, "Role updated successfully"));

        } catch (Exception e) {
            log.error("Error updating role {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update role"));
        }
    }

    /**
     * Delete role (only if no users are assigned)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        try {
            Optional<Role> roleOpt = roleRepository.findById(id);
            if (roleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notFound("Role not found"));
            }

            Role role = roleOpt.get();

            // Check if role has assigned users
            if (!role.getUsers().isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.conflict("Cannot delete role with assigned users"));
            }

            roleRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Role deleted successfully"));

        } catch (Exception e) {
            log.error("Error deleting role {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete role"));
        }
    }

    /**
     * Get users by role
     */
    @GetMapping("/{id}/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<com.pw.bakery.flow.dto.UserResponse>>> getUsersByRole(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Optional<Role> roleOpt = roleRepository.findById(id);
            if (roleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notFound("Role not found"));
            }

            Role role = roleOpt.get();
            List<com.pw.bakery.flow.dto.UserResponse> userResponses = role.getUsers().stream()
                    .map(com.pw.bakery.flow.dto.UserResponse::fromPublic)
                    .collect(Collectors.toList());

            // Simple pagination for demo (in production, use proper pagination)
            int start = page * size;
            int end = Math.min(start + size, userResponses.size());

            if (start >= userResponses.size()) {
                userResponses = java.util.Collections.emptyList();
            } else {
                userResponses = userResponses.subList(start, end);
            }

            return ResponseEntity.ok(ApiResponse.success(userResponses,
                String.format("Retrieved %d users for role %s", userResponses.size(), role.getName().getDisplayName())));

        } catch (Exception e) {
            log.error("Error retrieving users for role {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve users for role"));
        }
    }

    /**
     * Get all available role names
     */
    @GetMapping("/names")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<Role.RoleName>>> getAllRoleNames() {
        try {
            List<Role.RoleName> roleNames = java.util.Arrays.asList(Role.RoleName.values());
            return ResponseEntity.ok(ApiResponse.success(roleNames, "Role names retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving role names: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve role names"));
        }
    }
}
