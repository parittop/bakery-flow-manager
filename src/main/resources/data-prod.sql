-- Production Data Initialization for Bakery Flow Manager
-- This file creates essential data for production environment
-- Only includes minimal required data for system startup

-- Insert default roles (required for system operation)
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Full system access including user management'),
('MANAGER', 'Can manage operations, inventory, and view reports'),
('BAKER', 'Can manage production workflows and view assigned tasks'),
('CASHIER', 'Can handle orders and payments'),
('INVENTORY', 'Can manage stock and inventory');

-- Create initial admin user (password: admin123)
-- This should be changed immediately after first login
INSERT INTO users (username, email, password, first_name, last_name, phone_number, employee_id, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at) VALUES
('admin', 'admin@bakery.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'System', 'Administrator', '0000000000', 'ADMIN001', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- Production setup notes:
-- 1. Change the default admin password after first login
-- 2. Create additional users through the application interface
-- 3. Set up proper email configuration for notifications
-- 4. Configure backup procedures
-- 5. Set up monitoring and alerting
