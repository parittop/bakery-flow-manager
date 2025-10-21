package com.pw.bakery.flow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for Role API responses
 * Contains role information with user count
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResponse {

    private Long id;
    private String name;
    private String displayName;
    private String description;
    private Integer userCount;
    private Set<UserResponse> users;

    /**
     * Create RoleResponse from Role entity
     */
    public static RoleResponse from(com.pw.bakery.flow.domain.model.Role role) {
        if (role == null) {
            return null;
        }

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName().name())
                .displayName(role.getName().getDisplayName())
                .description(role.getDescription())
                .userCount(role.getUsers() != null ? role.getUsers().size() : 0)
                .build();
    }

    /**
     * Create RoleResponse with user details
     */
    public static RoleResponse fromWithUsers(com.pw.bakery.flow.domain.model.Role role) {
        if (role == null) {
            return null;
        }

        Set<UserResponse> userResponses = role.getUsers().stream()
                .map(UserResponse::fromPublic)
                .collect(Collectors.toSet());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName().name())
                .displayName(role.getName().getDisplayName())
                .description(role.getDescription())
                .userCount(role.getUsers() != null ? role.getUsers().size() : 0)
                .users(userResponses)
                .build();
    }
}
