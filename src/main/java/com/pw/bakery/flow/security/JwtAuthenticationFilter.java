package com.pw.bakery.flow.security;

import com.pw.bakery.flow.config.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Authentication Filter
 * Intercepts HTTP requests and validates JWT tokens
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;
    private final JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            if (
                StringUtils.hasText(jwt) && jwtTokenService.isAccessToken(jwt)
            ) {
                String username = jwtTokenService.extractUsername(jwt);

                if (
                    username != null &&
                    SecurityContextHolder.getContext().getAuthentication() ==
                    null
                ) {
                    UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                    if (jwtTokenService.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(
                                request
                            )
                        );

                        SecurityContextHolder.getContext().setAuthentication(
                            authentication
                        );

                        log.debug("Set authentication for user: {}", username);
                    } else {
                        log.debug("Invalid JWT token for user: {}", username);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from request
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(jwtProperties.getHeaderString());

        if (
            StringUtils.hasText(headerAuth) &&
            headerAuth.startsWith(jwtProperties.getTokenPrefix())
        ) {
            return headerAuth.substring(
                jwtProperties.getTokenPrefix().length()
            );
        }

        // Also try to get token from cookie
        String tokenCookie = getCookieValue(
            request,
            jwtProperties.getAccessTokenCookie()
        );
        if (StringUtils.hasText(tokenCookie)) {
            return tokenCookie;
        }

        // Also try to get token from query parameter (for WebSocket connections)
        String tokenParam = request.getParameter(
            jwtProperties.getAccessTokenParam()
        );
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

        return null;
    }

    /**
     * Get cookie value by name
     */
    private String getCookieValue(
        HttpServletRequest request,
        String cookieName
    ) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Check if request should be skipped from JWT validation
     */
    private boolean shouldSkip(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip authentication for public endpoints
        return (
            path.startsWith("/api/auth/") ||
            path.startsWith("/api/public/") ||
            path.startsWith("/actuator/") ||
            path.startsWith("/h2-console/") ||
            path.startsWith("/swagger-ui/") ||
            path.startsWith("/v3/api-docs/") ||
            path.startsWith("/webjars/") ||
            path.startsWith("/swagger-resources/") ||
            path.startsWith("/configuration/") ||
            path.startsWith("/v2/api-docs/") ||
            path.equals("/favicon.ico") ||
            path.equals("/error")
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)
        throws ServletException {
        return shouldSkip(request);
    }
}
