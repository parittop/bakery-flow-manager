package com.pw.bakery.flow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * JWT Configuration Properties
 * Configures JWT token settings including expiration, secret, and refresh token settings
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Secret key for signing JWT tokens
     * Should be a strong, randomly generated string
     */
    private String secret = "mySecretKeyForBakeryFlowManagerShouldBeAtLeast256BitsLong";

    /**
     * Access token expiration time
     */
    private Duration accessTokenExpiration = Duration.ofHours(1);

    /**
     * Refresh token expiration time
     */
    private Duration refreshTokenExpiration = Duration.ofDays(7);

    /**
     * Token issuer
     */
    private String issuer = "bakery-flow-manager";

    /**
     * Clock skew for token validation (to account for small time differences)
     */
    private Duration clockSkew = Duration.ofMinutes(1);

    /**
     * Whether to remember me functionality is enabled
     */
    private boolean rememberMeEnabled = true;

    /**
     * Extended expiration time for remember me tokens
     */
    private Duration rememberMeExpiration = Duration.ofDays(30);

    /**
     * Token prefix for Authorization header
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Header name for JWT token
     */
    private String headerString = "Authorization";

    /**
     * Parameter name for refresh token
     */
    private String refreshTokenParam = "refresh_token";

    /**
     * Parameter name for access token
     */
    private String accessTokenParam = "access_token";

    /**
     * Cookie name for refresh token
     */
    private String refreshTokenCookie = "refresh_token";

    /**
     * Cookie name for access token
     */
    private String accessTokenCookie = "access_token";

    /**
     * Whether to use HTTP-only cookies for tokens
     */
    private boolean httpOnlyCookies = false;

    /**
     * Whether to use secure cookies (HTTPS only)
     */
    private boolean secureCookies = false;

    /**
     * Cookie domain (optional)
     */
    private String cookieDomain;

    /**
     * Cookie path
     */
    private String cookiePath = "/";

    /**
     * SameSite cookie attribute
     */
    private String sameSiteCookie = "Lax";

    /**
     * Maximum number of active refresh tokens per user
     */
    private int maxRefreshTokensPerUser = 5;
}
