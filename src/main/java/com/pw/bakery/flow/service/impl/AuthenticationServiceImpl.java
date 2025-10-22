package com.pw.bakery.flow.service.impl;

import com.pw.bakery.flow.domain.model.Role;
import com.pw.bakery.flow.domain.model.User;
import com.pw.bakery.flow.repository.UserRepository;
import com.pw.bakery.flow.security.JwtTokenService;
import com.pw.bakery.flow.service.AuthenticationService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service Implementation
 * Provides authentication, registration, and token management functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional
    public Map<String, Object> authenticate(
        String username,
        String password,
        boolean rememberMe
    ) {
        log.info("Authenticating user: {}", username);

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();
            User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtTokenService.generateAccessToken(user);
            String refreshToken = jwtTokenService.generateRefreshToken(user);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put(
                "expiresIn",
                jwtTokenService.getAccessTokenExpirationMs()
            );
            response.put("user", buildUserResponse(user));

            log.info("User authenticated successfully: {}", username);
            return response;
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", username, e);
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public Map<String, Object> registerUser(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        String employeeId,
        String phoneNumber
    ) {
        log.info("Registering new user: {}", username);

        // Check if user already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user
        Set<Role> roles = new HashSet<>();
        Role userRole = Role.builder().name(Role.RoleName.BAKER).build();
        roles.add(userRole);

        User newUser = User.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .firstName(firstName)
            .lastName(lastName)
            .employeeId(employeeId)
            .phoneNumber(phoneNumber)
            .enabled(true)
            .roles(roles)
            .build();

        User savedUser = userRepository.save(newUser);

        // Auto-login after registration
        return authenticate(username, password, false);
    }

    @Override
    @Transactional
    public Map<String, Object> refreshToken(String refreshToken) {
        log.debug("Refreshing token");

        try {
            String username = jwtTokenService.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(
                username
            );

            if (jwtTokenService.validateToken(refreshToken, userDetails)) {
                User user = userRepository
                    .findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

                String newAccessToken = jwtTokenService.generateAccessToken(
                    user
                );

                Map<String, Object> response = new HashMap<>();
                response.put("accessToken", newAccessToken);
                response.put("tokenType", "Bearer");
                response.put(
                    "expiresIn",
                    jwtTokenService.getAccessTokenExpirationMs()
                );
                response.put("user", buildUserResponse(user));

                log.debug(
                    "Token refreshed successfully for user: {}",
                    username
                );
                return response;
            } else {
                throw new RuntimeException("Invalid refresh token");
            }
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new RuntimeException("Invalid or expired refresh token");
        }
    }

    @Override
    @Transactional
    public void changePassword(
        String username,
        String currentPassword,
        String newPassword
    ) {
        log.info("Changing password for user: {}", username);

        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", username);
    }

    @Override
    @Transactional
    public void logout(String username) {
        log.info("Logging out user: {}", username);
        // In a real implementation, you might want to invalidate the token
        // For JWT, this is typically handled by token blacklisting or short expiration times
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository
            .findByUsername(username)
            .orElseThrow(() ->
                new RuntimeException("User not found: " + username)
            );
    }

    /**
     * Build user response map
     */
    private Map<String, Object> buildUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
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
        return userResponse;
    }
}
