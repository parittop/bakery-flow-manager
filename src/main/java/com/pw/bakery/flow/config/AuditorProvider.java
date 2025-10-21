package com.pw.bakery.flow.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * AuditorAware implementation for JPA auditing
 * Provides the current user's ID for createdBy and updatedBy fields
 */
@Component
public class AuditorProvider implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        // Get user ID from authenticated user
        Object principal = authentication.getPrincipal();

        if (principal instanceof com.pw.bakery.flow.domain.model.User) {
            return Optional.of(((com.pw.bakery.flow.domain.model.User) principal).getId());
        }

        // Handle case where principal is just username (String)
        if (principal instanceof String) {
            // In this case, we'd need to fetch the user from database
            // For simplicity, we'll return empty and handle null values in entities
            return Optional.empty();
        }

        return Optional.empty();
    }
}
