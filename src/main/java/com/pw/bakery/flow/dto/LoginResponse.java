package com.pw.bakery.flow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for login responses
 * Contains authentication tokens and user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private Map<String, Object> user;

    /**
     * Create login response with tokens
     */
    public static LoginResponse of(String accessToken, String refreshToken, String tokenType,
                                  Long expiresIn, Map<String, Object> user) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }

    /**
     * Create login response without refresh token (for short-lived sessions)
     */
    public static LoginResponse of(String accessToken, String tokenType,
                                  Long expiresIn, Map<String, Object> user) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }

    /**
     * Get token as Authorization header value
     */
    public String getAuthorizationHeader() {
        return tokenType + " " + accessToken;
    }

    /**
     * Check if response is valid
     */
    public boolean isValid() {
        return accessToken != null && !accessToken.trim().isEmpty() &&
               tokenType != null && !tokenType.trim().isEmpty() &&
               expiresIn != null && expiresIn > 0 &&
               user != null && !user.isEmpty();
    }
}
