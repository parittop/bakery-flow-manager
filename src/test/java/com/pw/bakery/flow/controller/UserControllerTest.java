package com.pw.bakery.flow.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.bakery.flow.domain.model.Role;
import com.pw.bakery.flow.domain.model.User;
import com.pw.bakery.flow.dto.UserCreateRequest;
import com.pw.bakery.flow.dto.UserResponse;
import com.pw.bakery.flow.dto.UserUpdateRequest;
import com.pw.bakery.flow.repository.RoleRepository;
import com.pw.bakery.flow.repository.UserRepository;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration test for UserController
 * Tests all API endpoints with security and validation
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;
    private Role adminRole;
    private Role managerRole;
    private Role bakerRole;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        // Clean database
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create test roles
        adminRole = Role.builder()
            .name(Role.RoleName.ADMIN)
            .description("Full system access")
            .users(new HashSet<>())
            .build();

        managerRole = Role.builder()
            .name(Role.RoleName.MANAGER)
            .description("Operations manager")
            .users(new HashSet<>())
            .build();

        bakerRole = Role.builder()
            .name(Role.RoleName.BAKER)
            .description("Production baker")
            .users(new HashSet<>())
            .build();

        adminRole = roleRepository.save(adminRole);
        managerRole = roleRepository.save(managerRole);
        bakerRole = roleRepository.save(bakerRole);

        // Create test user
        testUser = User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password123")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("0812345678")
            .employeeId("TEST001")
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .roles(new HashSet<>(Set.of(bakerRole)))
            .build();

        testUser = userRepository.save(testUser);
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testGetAllUsers() throws Exception {
        mockMvc
            .perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].username").value("testuser"))
            .andExpect(jsonPath("$.data[0].email").value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = { "BAKER" })
    void testGetAllUsersForbidden() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "BAKER" })
    void testGetUserById_Owner() throws Exception {
        mockMvc
            .perform(get("/api/users/{id}", testUser.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testGetUserById_Admin() throws Exception {
        mockMvc
            .perform(get("/api/users/{id}", testUser.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = { "MANAGER" })
    void testGetUserById_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/users/{id}", 999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "BAKER" })
    void testGetCurrentUser() throws Exception {
        mockMvc
            .perform(get("/api/users/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void testGetCurrentUser_Unauthenticated() throws Exception {
        mockMvc
            .perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testCreateUser_Success() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
            .username("newuser")
            .email("newuser@example.com")
            .password("password123")
            .firstName("New")
            .lastName("User")
            .phoneNumber("0823456789")
            .employeeId("NEW001")
            .roleIds(Set.of(adminRole.getId()))
            .build();

        mockMvc
            .perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("newuser"))
            .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
            .andExpect(jsonPath("$.data.employeeId").value("NEW001"));
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testCreateUser_DuplicateUsername() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
            .username("testuser") // Already exists
            .email("different@example.com")
            .password("password123")
            .firstName("Different")
            .lastName("User")
            .employeeId("DIFF001")
            .roleIds(Set.of(adminRole.getId()))
            .build();

        mockMvc
            .perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("CONFLICT"));
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testCreateUser_ValidationError() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
            .username("") // Invalid
            .email("invalid-email") // Invalid
            .password("123") // Too short
            .firstName("")
            .lastName("")
            .roleIds(Set.of()) // Empty
            .build();

        mockMvc
            .perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = { "BAKER" })
    void testCreateUser_Forbidden() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
            .username("newuser")
            .email("newuser@example.com")
            .password("password123")
            .firstName("New")
            .lastName("User")
            .employeeId("NEW001")
            .roleIds(Set.of(bakerRole.getId()))
            .build();

        mockMvc
            .perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testUpdateUser_Success() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
            .firstName("Updated")
            .lastName("Name")
            .phoneNumber("0998765432")
            .roleIds(Set.of(managerRole.getId()))
            .build();

        mockMvc
            .perform(
                put("/api/users/{id}", testUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.firstName").value("Updated"))
            .andExpect(jsonPath("$.data.lastName").value("Name"))
            .andExpect(jsonPath("$.data.phoneNumber").value("0998765432"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "BAKER" })
    void testUpdateUser_Owner() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
            .firstName("Self")
            .lastName("Updated")
            .build();

        mockMvc
            .perform(
                put("/api/users/{id}", testUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.firstName").value("Self"))
            .andExpect(jsonPath("$.data.lastName").value("Updated"));
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testUpdateUser_NotFound() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
            .firstName("Updated")
            .build();

        mockMvc
            .perform(
                put("/api/users/{id}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testDeleteUser_Success() throws Exception {
        Long userId = testUser.getId();

        mockMvc
            .perform(delete("/api/users/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(
                jsonPath("$.message").value("User deleted successfully")
            );

        // Verify user is deleted
        mockMvc
            .perform(get("/api/users/{id}", userId))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = { "MANAGER" })
    void testDeleteUser_Forbidden() throws Exception {
        mockMvc
            .perform(delete("/api/users/{id}", testUser.getId()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testSearchUsers_Success() throws Exception {
        mockMvc
            .perform(
                get("/api/users/search")
                    .param("username", "test")
                    .param("enabled", "true")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testSearchUsers_WithInvalidRole() throws Exception {
        mockMvc
            .perform(get("/api/users/search").param("roleName", "INVALID_ROLE"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testUpdateUserStatus_Enable() throws Exception {
        // First disable the user
        testUser.setEnabled(false);
        userRepository.save(testUser);

        mockMvc
            .perform(
                patch("/api/users/{id}/status", testUser.getId()).param(
                    "enabled",
                    "true"
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.enabled").value(true))
            .andExpect(
                jsonPath("$.message").value("User enabled successfully")
            );
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testUpdateUserStatus_Disable() throws Exception {
        mockMvc
            .perform(
                patch("/api/users/{id}/status", testUser.getId()).param(
                    "enabled",
                    "false"
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.enabled").value(false))
            .andExpect(
                jsonPath("$.message").value("User disabled successfully")
            );
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testGetAllUsers_WithPagination() throws Exception {
        // Create additional users
        for (int i = 1; i <= 5; i++) {
            User user = User.builder()
                .username("user" + i)
                .email("user" + i + "@example.com")
                .password("password123")
                .firstName("User")
                .lastName(String.valueOf(i))
                .employeeId("USER00" + i)
                .roles(new HashSet<>(Set.of(bakerRole)))
                .build();
            userRepository.save(user);
        }

        mockMvc
            .perform(
                get("/api/users")
                    .param("page", "0")
                    .param("size", "3")
                    .param("sortBy", "username")
                    .param("sortDir", "asc")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testGetUserById_WithRoles() throws Exception {
        mockMvc
            .perform(get("/api/users/{id}", testUser.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.roles").isArray())
            .andExpect(jsonPath("$.data.roles", hasSize(1)))
            .andExpect(jsonPath("$.data.roles[0].name").value("BAKER"))
            .andExpect(jsonPath("$.data.roles[0].displayName").value("Baker"));
    }
}
