package com.pw.bakery.flow.security;

import com.pw.bakery.flow.config.JwtProperties;
import com.pw.bakery.flow.domain.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * JWT Token Service
 * Handles JWT token generation, validation, and parsing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate access token for user
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put(
            "roles",
            user
                .getRoles()
                .stream()
                .map(role -> role.getName().name())
                .toArray(String[]::new)
        );
        claims.put("employeeId", user.getEmployeeId());
        claims.put("tokenType", "access");

        return createToken(
            claims,
            user.getUsername(),
            jwtProperties.getAccessTokenExpiration().toMillis()
        );
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("tokenType", "refresh");

        return createToken(
            claims,
            user.getUsername(),
            jwtProperties.getRefreshTokenExpiration().toMillis()
        );
    }

    /**
     * Generate remember me token with extended expiration
     */
    public String generateRememberMeToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("tokenType", "remember_me");

        return createToken(
            claims,
            user.getUsername(),
            jwtProperties.getRememberMeExpiration().toMillis()
        );
    }

    /**
     * Create JWT token with specified claims and expiration
     */
    private String createToken(
        Map<String, Object> claims,
        String subject,
        long expiration
    ) {
        // Use LocalDateTime for consistent local time handling
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validity = now.plus(expiration, ChronoUnit.MILLIS);

        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuer(jwtProperties.getIssuer())
            .issuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
            .expiration(
                Date.from(validity.atZone(ZoneId.systemDefault()).toInstant())
            )
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    /**
     * Extract roles from token
     */
    public String[] extractRoles(String token) {
        return extractClaim(token, claims ->
            claims.get("roles", String[].class)
        );
    }

    /**
     * Extract token type from token
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims ->
            claims.get("tokenType", String.class)
        );
    }

    /**
     * Extract employee ID from token
     */
    public String extractEmployeeId(String token) {
        return extractClaim(token, claims ->
            claims.get("employeeId", String.class)
        );
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(
        String token,
        Function<Claims, T> claimsResolver
    ) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.warn(
                "JWT token signature validation failed: {}",
                e.getMessage()
            );
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn(
                "JWT token compact of handler are invalid: {}",
                e.getMessage()
            );
            throw e;
        }
    }

    /**
     * Validate token against user details
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (
                username.equals(userDetails.getUsername()) &&
                !isTokenExpired(token)
            );
        } catch (JwtException e) {
            log.debug("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            final Date expiration = extractExpiration(token);
            LocalDateTime expDateTime = LocalDateTime.ofInstant(
                expiration.toInstant(),
                ZoneId.systemDefault()
            );
            return expDateTime.isBefore(LocalDateTime.now());
        } catch (ExpiredJwtException e) {
            log.debug("Error checking token expiration: {}", e.getMessage());
            return true; // Treat invalid tokens as expired
        } catch (JwtException e) {
            log.debug("Error checking token expiration: {}", e.getMessage());
            return true; // Treat invalid tokens as expired
        }
    }

    /**
     * Get access token expiration time in milliseconds
     */
    public long getAccessTokenExpirationMs() {
        return jwtProperties.getAccessTokenExpiration().toMillis();
    }

    /**
     * Debug method to check token validation
     */
    public void debugTokenValidation(String token) {
        try {
            Date expiration = extractExpiration(token);
            LocalDateTime expDateTime = LocalDateTime.ofInstant(
                expiration.toInstant(),
                ZoneId.systemDefault()
            );
            LocalDateTime now = LocalDateTime.now();

            log.debug("Token validation debug:");
            log.debug("  Current time: {}", now);
            log.debug("  Token expires: {}", expDateTime);
            log.debug("  Is expired: {}", expDateTime.isBefore(now));
            log.debug(
                "  Time until expiry: {}",
                ChronoUnit.SECONDS.between(now, expDateTime)
            );
        } catch (Exception e) {
            log.debug("Error in token validation debug: {}", e.getMessage());
        }
    }

    /**
     * Check if token is refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(extractTokenType(token));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Check if token is access token
     */
    public boolean isAccessToken(String token) {
        try {
            return "access".equals(extractTokenType(token));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Check if token is remember me token
     */
    public boolean isRememberMeToken(String token) {
        try {
            return "remember_me".equals(extractTokenType(token));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Get remaining validity time for token
     */
    public long getRemainingValidity(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (JwtException e) {
            return 0;
        }
    }

    /**
     * Parse token without validation (for debugging/logging)
     */
    public Claims parseTokenUnsafe(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException e) {
            log.debug("Failed to parse token: {}", e.getMessage());
            throw e;
        }
    }
}
