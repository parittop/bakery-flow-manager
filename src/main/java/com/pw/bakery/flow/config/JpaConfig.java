package com.pw.bakery.flow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA Configuration for Bakery Flow Manager
 * Enables JPA auditing and repository scanning
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableJpaRepositories(basePackages = "com.pw.bakery.flow.repository")
public class JpaConfig {
    // JPA auditing configuration will be handled by AuditorProvider bean
    // Repository scanning enabled for com.pw.bakery.flow.repository package
}
