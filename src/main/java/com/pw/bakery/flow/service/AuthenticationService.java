package com.pw.bakery.flow.service;

import java.util.Map;

/**
 * Authentication Service Interface
 * Handles user authentication, registration, and token management
 */
public interface AuthenticationService {
    /**
     * Authenticate user with username and password
     * @param username the username
     * @param password the password
     * @param rememberMe whether to remember the user
     * @return authentication result with tokens and user info
     */
    Map<String, Object> authenticate(
        String username,
        String password,
        boolean rememberMe
    );

    /**
     * Register a new user
     * @param username the username
     * @param email the email
     * @param password the password
     * @param firstName the first name
     * @param lastName the last name
     * @param employeeId the employee ID
     * @param phoneNumber the phone number
     * @return registration result with tokens and user info
     */
    Map<String, Object> registerUser(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        String employeeId,
        String phoneNumber
    );

    /**
     * Refresh access token using refresh token
     * @param refreshToken the refresh token
     * @return new authentication result with updated tokens
     */
    Map<String, Object> refreshToken(String refreshToken);

    /**
     * Change user password
     * @param username the username
     * @param currentPassword the current password
     * @param newPassword the new password
     */
    void changePassword(
        String username,
        String currentPassword,
        String newPassword
    );

    /**
     * Logout user
     * @param username the username
     */
    void logout(String username);

    /**
     * Check if user exists
     * @param username the username
     * @return true if user exists, false otherwise
     */
    boolean userExists(String username);

    /**
     * Check if email exists
     * @param email the email
     * @return true if email exists, false otherwise
     */
    boolean emailExists(String email);

    /**
     * Get user by username
     * @param username the username
     * @return the user
     */
    com.pw.bakery.flow.domain.model.User getUserByUsername(String username);
}
