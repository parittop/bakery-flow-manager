package com.pw.bakery.flow.controller;

import com.pw.bakery.flow.dto.ApiResponse;
import com.pw.bakery.flow.dto.ChangePasswordRequest;
import com.pw.bakery.flow.dto.LoginRequest;
import com.pw.bakery.flow.dto.LoginResponse;
import com.pw.bakery.flow.dto.RefreshTokenRequest;
import com.pw.bakery.flow.dto.RegisterRequest;
import com.pw.bakery.flow.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user authentication, registration, and token management
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(
    name = "Authentication",
    description = "User authentication and authorization APIs"
)
public class AuthController {

    private final AuthenticationService authService;

    /**
     * Authenticate user and return tokens
     */
    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user with username and password"
    )
    @ApiResponses(
        value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Login successful"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Invalid credentials"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid request"
            ),
        }
    )
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        try {
            Map<String, Object> authResult = authService.authenticate(
                request.getUsername(),
                request.getPassword(),
                request.isRememberMe()
            );

            LoginResponse response = LoginResponse.builder()
                .accessToken((String) authResult.get("accessToken"))
                .refreshToken((String) authResult.get("refreshToken"))
                .tokenType((String) authResult.get("tokenType"))
                .expiresIn((Long) authResult.get("expiresIn"))
                .user((Map<String, Object>) authResult.get("user"))
                .build();

            return ResponseEntity.ok(
                ApiResponse.success(response, "Login successful")
            );
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.unauthorized("Invalid username or password")
            );
        }
    }

    /**
     * Register new user
     */
    @PostMapping("/register")
    @Operation(
        summary = "User registration",
        description = "Register a new user account"
    )
    @ApiResponses(
        value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "Registration successful"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid request data"
            ),
        }
    )
    public ResponseEntity<ApiResponse<LoginResponse>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        try {
            Map<String, Object> authResult = authService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmployeeId(),
                request.getPhoneNumber()
            );

            LoginResponse response = LoginResponse.builder()
                .accessToken((String) authResult.get("accessToken"))
                .refreshToken((String) authResult.get("refreshToken"))
                .tokenType((String) authResult.get("tokenType"))
                .expiresIn((Long) authResult.get("expiresIn"))
                .user((Map<String, Object>) authResult.get("user"))
                .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                    response,
                    "Registration successful",
                    HttpStatus.CREATED
                )
            );
        } catch (IllegalArgumentException e) {
            log.warn("Registration error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.validationError(e.getMessage())
            );
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Registration failed")
            );
        }
    }

    /**
     * Refresh access token
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh token",
        description = "Generate new access token using refresh token"
    )
    @ApiResponses(
        value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Token refreshed successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Invalid refresh token"
            ),
        }
    )
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        try {
            Map<String, Object> authResult = authService.refreshToken(
                request.getRefreshToken()
            );

            LoginResponse response = LoginResponse.builder()
                .accessToken((String) authResult.get("accessToken"))
                .tokenType((String) authResult.get("tokenType"))
                .expiresIn((Long) authResult.get("expiresIn"))
                .user((Map<String, Object>) authResult.get("user"))
                .build();

            return ResponseEntity.ok(
                ApiResponse.success(response, "Token refreshed successfully")
            );
        } catch (Exception e) {
            log.error("Token refresh error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.unauthorized("Invalid or expired refresh token")
            );
        }
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Change password",
        description = "Change authenticated user's password"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(
        value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Password changed successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid current password"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Unauthorized"
            ),
        }
    )
    public ResponseEntity<ApiResponse<Void>> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        try {
            authService.changePassword(
                userDetails.getUsername(),
                request.getCurrentPassword(),
                request.getNewPassword()
            );

            return ResponseEntity.ok(
                ApiResponse.success(null, "Password changed successfully")
            );
        } catch (Exception e) {
            log.error("Password change error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Logout user
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
        @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        try {
            authService.logout(userDetails.getUsername());
            return ResponseEntity.ok(
                ApiResponse.success(null, "Logout successful")
            );
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Logout failed")
            );
        }
    }

    /**
     * Validate token
     */
    @GetMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
        @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        Map<String, Object> response = Map.of(
            "valid",
            true,
            "username",
            userDetails.getUsername(),
            "authorities",
            userDetails
                .getAuthorities()
                .stream()
                .map(auth -> Map.of("authority", auth.getAuthority()))
                .toList()
        );

        return ResponseEntity.ok(
            ApiResponse.success(response, "Token is valid")
        );
    }

    /**
     * Check if username is available
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<
        ApiResponse<Map<String, Boolean>>
    > checkUsernameAvailability(@PathVariable String username) {
        boolean available = !authService.userExists(username);
        Map<String, Boolean> response = Map.of("available", available);

        return ResponseEntity.ok(
            ApiResponse.success(
                response,
                available
                    ? "Username is available"
                    : "Username is not available"
            )
        );
    }

    /**
     * Check if email is available
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<
        ApiResponse<Map<String, Boolean>>
    > checkEmailAvailability(@PathVariable String email) {
        boolean available = !authService.emailExists(email);
        Map<String, Boolean> response = Map.of("available", available);

        return ResponseEntity.ok(
            ApiResponse.success(
                response,
                available ? "Email is available" : "Email is not available"
            )
        );
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
        @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        try {
            var user = authService.getUserByUsername(userDetails.getUsername());
            Map<String, Object> userResponse = new java.util.HashMap<>();
            userResponse.put("id", user.getId());
            userResponse.put("username", user.getUsername());
            userResponse.put("email", user.getEmail());
            userResponse.put("firstName", user.getFirstName());
            userResponse.put("lastName", user.getLastName());
            userResponse.put("fullName", user.getFullName());
            userResponse.put("phoneNumber", user.getPhoneNumber());
            userResponse.put("employeeId", user.getEmployeeId());
            userResponse.put("enabled", user.isEnabled());
            userResponse.put("lastLogin", user.getLastLogin());
            userResponse.put(
                "roles",
                user
                    .getRoles()
                    .stream()
                    .map(role ->
                        Map.of(
                            "id",
                            role.getId(),
                            "name",
                            role.getName().name(),
                            "displayName",
                            role.getName().getDisplayName()
                        )
                    )
                    .toList()
            );

            return ResponseEntity.ok(
                ApiResponse.success(
                    userResponse,
                    "Current user retrieved successfully"
                )
            );
        } catch (Exception e) {
            log.error("Error retrieving current user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to retrieve current user")
            );
        }
    }
}
