-- Development Data Initialization for Bakery Flow Manager
-- This file creates default roles and users for development environment

-- Insert default roles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Full system access including user management'),
('MANAGER', 'Can manage operations, inventory, and view reports'),
('BAKER', 'Can manage production workflows and view assigned tasks'),
('CASHIER', 'Can handle orders and payments'),
('INVENTORY', 'Can manage stock and inventory');

-- Insert default users with encrypted passwords (BCrypt)
-- Passwords are "admin123", "manager123", "baker123", "cashier123", "inventory123"
INSERT INTO users (username, email, password, first_name, last_name, phone_number, employee_id, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at) VALUES
('admin', 'admin@bakery.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin', 'User', '0812345678', 'EMP001', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('manager', 'manager@bakery.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Manager', 'User', '0823456789', 'EMP002', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('baker', 'baker@bakery.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Baker', 'User', '0834567890', 'EMP003', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cashier', 'cashier@bakery.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Cashier', 'User', '0845678901', 'EMP004', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('inventory', 'inventory@bakery.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Inventory', 'Staff', '0856789012', 'EMP005', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES
-- Admin gets ADMIN role
(1, 1),
-- Manager gets MANAGER role
(2, 2),
-- Baker gets BAKER role
(3, 3),
-- Cashier gets CASHIER role
(4, 4),
-- Inventory staff gets INVENTORY role
(5, 5);

-- Add some additional test users with multiple roles
INSERT INTO users (username, email, password, first_name, last_name, phone_number, employee_id, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at) VALUES
('supervisor', 'supervisor@bakery.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Supervisor', 'User', '0867890123', 'EMP006', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('headbaker', 'headbaker@bakery.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Head', 'Baker', '0878901234', 'EMP007', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Supervisor gets both MANAGER and BAKER roles
INSERT INTO user_roles (user_id, role_id) VALUES (6, 2), (6, 3);

-- Head Baker gets BAKER and INVENTORY roles
INSERT INTO user_roles (user_id, role_id) VALUES (7, 3), (7, 5);

-- Add a disabled user for testing
INSERT INTO users (username, email, password, first_name, last_name, phone_number, employee_id, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at) VALUES
('disableduser', 'disabled@bakery.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Disabled', 'User', '0889012345', 'EMP008', false, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Disabled user gets CASHIER role
INSERT INTO user_roles (user_id, role_id) VALUES (8, 4);
