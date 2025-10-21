package com.pw.bakery.flow.controller;

import com.pw.bakery.flow.dto.ApiResponse;
import com.pw.bakery.flow.dto.UserCreateRequest;
import com.pw.bakery.flow.dto.UserResponse;
import com.pw.bakery.flow.dto.UserUpdateRequest;
import com.pw.bakery.flow.domain.model.User;
import com.pw.bakery.flow.repository.RoleRepository;
import com.pw.bakery.flow.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for User Management
 * Provides CRUD operations for user accounts
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users with pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<User> users = userRepository.findAll(pageable);
            List<UserResponse> userResponses = users.getContent().stream()
                    .map(UserResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(userResponses,
                String.format("Retrieved %d users", users.getTotalElements())));

        } catch (Exception e) {
            log.error("Error retrieving users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve users"));
        }
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @userRepository.findById(#id).get().username == authentication.name")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notFound("User not found"));
            }

            UserResponse userResponse = UserResponse.from(userOpt.get());
            return ResponseEntity.ok(ApiResponse.success(userResponse, "User retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve user"));
        }
    }

    /**
     * Get current authenticated user
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.unauthorized("User not authenticated"));
            }

            Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notFound("Current user not found"));
            }

            UserResponse userResponse = UserResponse.from(userOpt.get());
            return ResponseEntity.ok(ApiResponse.success(userResponse, "Current user retrieved successfully"));

        } catch (Exception e) {
            log.error("Error retrieving current user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve current user"));
        }
    }

    /**
     * Create new user
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        try {
            // Check if username already exists
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.conflict("Username already exists"));
            }

            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.conflict("Email already exists"));
            }

            // Check if employee ID already exists
            if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.conflict("Employee ID already exists"));
            }

            // Validate roles
            if (request.getRoleIds().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.validationError("At least one role is required"));
            }

            var roles = roleRepository.findAllById(request.getRoleIds());
            if (roles.size() != request.getRoleIds().size()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.validationError("One or more roles not found"));
            }

            // Create user
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phoneNumber(request.getPhoneNumber())
                    .employeeId(request.getEmployeeId())
                    .enabled(request.getEnabled())
                    .accountNonExpired(request.getAccountNonExpired())
                    .accountNonLocked(request.getAccountNonLocked())
                    .credentialsNonExpired(request.getCredentialsNonExpired())
                    .roles(new java.util.HashSet<>(roles))
                    .build();

            User savedUser = userRepository.save(user);
            UserResponse userResponse = UserResponse.from(savedUser);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(userResponse, "User created successfully", HttpStatus.CREATED));

        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create user"));
        }
    }

    /**
     * Update existing user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @userRepository.findById(#id).get().username == authentication.name")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notFound("User not found"));
            }

            User user = userOpt.get();

            // Check username uniqueness if changed
            if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
                if (userRepository.existsByUsername(request.getUsername())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(ApiResponse.conflict("Username already exists"));
                }
                user.setUsername(request.getUsername());
            }

            // Check email uniqueness if changed
            if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(ApiResponse.conflict("Email already exists"));
                }
                user.setEmail(request.getEmail());
            }

            // Check employee ID uniqueness if changed
            if (request.getEmployeeId() != null && !request.getEmployeeId().equals(user.getEmployeeId())) {
                if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(ApiResponse.conflict("Employee ID already exists"));
                }
                user.setEmployeeId(request.getEmployeeId());
            }

            // Update roles if provided
            if (request.getRoleIds() != null) {
                var roles = roleRepository.findAllById(request.getRoleIds());
                if (roles.size() != request.getRoleIds().size()) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.validationError("One or more roles not found"));
                }
                user.setRoles(new java.util.HashSet<>(roles));
            }

            // Update other fields
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
            if (request.getLastName() != null) user.setLastName(request.getLastName());
            if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
            if (request.getEnabled() != null) user.setEnabled(request.getEnabled());
            if (request.getAccountNonExpired() != null) user.setAccountNonExpired(request.getAccountNonExpired());
            if (request.getAccountNonLocked() != null) user.setAccountNonLocked(request.getAccountNonLocked());
            if (request.getCredentialsNonExpired() != null) user.setCredentialsNonExpired(request.getCredentialsNonExpired());

            User updatedUser = userRepository.save(user);
            UserResponse userResponse = UserResponse.from(updatedUser);

            return ResponseEntity.ok(ApiResponse.success(userResponse, "User updated successfully"));

        } catch (Exception e) {
            log.error("Error updating user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update user"));
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            if (!userRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notFound("User not found"));
            }

            userRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));

        } catch (Exception e) {
            log.error("Error deleting user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete user"));
        }
    }

    /**
     * Search users by multiple criteria
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Boolean enabled) {
        try {
            com.pw.bakery.flow.domain.model.Role.RoleName roleEnum = null;
            if (roleName != null && !roleName.trim().isEmpty()) {
                try {
                    roleEnum = com.pw.bakery.flow.domain.model.Role.RoleName.valueOf(roleName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.validationError("Invalid role name: " + roleName));
                }
            }

            List<User> users = userRepository.searchUsers(
                    username, email, firstName, lastName, employeeId, roleEnum, enabled);

            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(userResponses,
                String.format("Found %d users matching criteria", userResponses.size())));

        } catch (Exception e) {
            log.error("Error searching users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search users"));
        }
    }

    /**
     * Enable/disable user
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notFound("User not found"));
            }

            User user = userOpt.get();
            user.setEnabled(enabled);
            User updatedUser = userRepository.save(user);
            UserResponse userResponse = UserResponse.from(updatedUser);

            String message = enabled ? "User enabled successfully" : "User disabled successfully";
            return ResponseEntity.ok(ApiResponse.success(userResponse, message));

        } catch (Exception e) {
            log.error("Error updating user status {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update user status"));
        }
    }
}
