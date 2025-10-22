package com.pw.bakery.flow.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for default users used in development and testing
 */
@Component
@ConfigurationProperties(prefix = "app.default.users")
public class DefaultUsersProperties {

    private UserProperties admin = new UserProperties();
    private UserProperties manager = new UserProperties();
    private UserProperties baker = new UserProperties();
    private UserProperties cashier = new UserProperties();
    private String defaultPassword = "password123";

    public static class UserProperties {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String employeeId;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    }

    // Getters and Setters
    public UserProperties getAdmin() { return admin; }
    public void setAdmin(UserProperties admin) { this.admin = admin; }

    public UserProperties getManager() { return manager; }
    public void setManager(UserProperties manager) { this.manager = manager; }

    public UserProperties getBaker() { return baker; }
    public void setBaker(UserProperties baker) { this.baker = baker; }

    public UserProperties getCashier() { return cashier; }
    public void setCashier(UserProperties cashier) { this.cashier = cashier; }

    public String getDefaultPassword() { return defaultPassword; }
    public void setDefaultPassword(String defaultPassword) { this.defaultPassword = defaultPassword; }
}
