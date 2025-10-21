package com.pw.bakery.flow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for User API responses
 * Contains user information without sensitive data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String employeeId;
    private Boolean enabled;
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLogin;

    private Set<RoleResponse> roles;
    private String createdBy;
    private String updatedBy;

    /**
     * Create UserResponse from User entity
     */
    public static UserResponse from(com.pw.bakery.flow.domain.model.User user) {
        if (user == null) {
            return null;
        }

        Set<RoleResponse> roleResponses = user
            .getRoles()
            .stream()
            .map(role ->
                RoleResponse.builder()
                    .id(role.getId())
                    .name(role.getName().name())
                    .description(role.getDescription())
                    .build()
            )
            .collect(Collectors.toSet());

        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fullName(user.getFullName())
            .phoneNumber(user.getPhoneNumber())
            .employeeId(user.getEmployeeId())
            .enabled(user.isEnabled())
            .accountNonExpired(user.isAccountNonExpired())
            .accountNonLocked(user.isAccountNonLocked())
            .credentialsNonExpired(user.isCredentialsNonExpired())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .lastLogin(user.getLastLogin())
            .roles(roleResponses)
            .createdBy(
                user.getCreatedBy() != null
                    ? "User ID: " + user.getCreatedBy()
                    : null
            )
            .updatedBy(
                user.getUpdatedBy() != null
                    ? "User ID: " + user.getUpdatedBy()
                    : null
            )
            .build();
    }

    /**
     * Create simplified UserResponse for public APIs
     */
    public static UserResponse fromPublic(
        com.pw.bakery.flow.domain.model.User user
    ) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fullName(user.getFullName())
            .employeeId(user.getEmployeeId())
            .roles(
                user
                    .getRoles()
                    .stream()
                    .map(role ->
                        RoleResponse.builder()
                            .id(role.getId())
                            .name(role.getName().name())
                            .displayName(role.getName().getDisplayName())
                            .build()
                    )
                    .collect(Collectors.toSet())
            )
            .build();
    }
}
