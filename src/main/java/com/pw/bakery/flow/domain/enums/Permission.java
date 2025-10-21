package com.pw.bakery.flow.domain.enums;

/**
 * Permission enum for fine-grained access control in bakery system
 * Each permission represents a specific action that can be performed
 */
public enum Permission {

    // User Management Permissions
    USER_READ("user:read", "View user information"),
    USER_WRITE("user:write", "Create and update users"),
    USER_DELETE("user:delete", "Delete users"),
    USER_ROLE_MANAGE("user:role:manage", "Assign and remove user roles"),

    // Product Management Permissions
    PRODUCT_READ("product:read", "View product information"),
    PRODUCT_WRITE("product:write", "Create and update products"),
    PRODUCT_DELETE("product:delete", "Delete products"),
    PRODUCT_PRICE_MANAGE("product:price:manage", "Update product prices"),

    // Inventory Management Permissions
    INVENTORY_READ("inventory:read", "View inventory levels"),
    INVENTORY_WRITE("inventory:write", "Update inventory quantities"),
    INVENTORY_ADJUST("inventory:adjust", "Make inventory adjustments"),
    INVENTORY_REPORT("inventory:report", "View inventory reports"),

    // Order Management Permissions
    ORDER_READ("order:read", "View order information"),
    ORDER_WRITE("order:write", "Create and update orders"),
    ORDER_DELETE("order:delete", "Cancel/delete orders"),
    ORDER_PAYMENT("order:payment", "Process payments"),
    ORDER_REFUND("order:refund", "Process refunds"),

    // Production Workflow Permissions
    PRODUCTION_READ("production:read", "View production workflows"),
    PRODUCTION_WRITE("production:write", "Create and update production workflows"),
    PRODUCTION_EXECUTE("production:execute", "Execute production tasks"),
    PRODUCTION_QUALITY("production:quality", "Quality control and approval"),

    // Financial Permissions
    FINANCIAL_READ("financial:read", "View financial reports"),
    FINANCIAL_REPORT("financial:report", "Generate financial reports"),
    FINANCIAL_EXPORT("financial:export", "Export financial data"),

    // System Administration Permissions
    SYSTEM_CONFIG("system:config", "Modify system configuration"),
    SYSTEM_BACKUP("system:backup", "Perform system backups"),
    SYSTEM_LOGS("system:logs", "View system logs"),

    // Reporting Permissions
    REPORT_SALES("report:sales", "View sales reports"),
    REPORT_PRODUCTION("report:production", "View production reports"),
    REPORT_INVENTORY("report:inventory", "View inventory reports"),
    REPORT_EMPLOYEE("report:employee", "View employee reports");

    private final String code;
    private final String description;

    Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Find permission by code
     */
    public static Permission fromCode(String code) {
        for (Permission permission : values()) {
            if (permission.getCode().equals(code)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown permission code: " + code);
    }

    /**
     * Get all permissions for a specific category
     */
    public static Permission[] getPermissionsByCategory(String category) {
        return java.util.Arrays.stream(values())
                .filter(p -> p.getCode().startsWith(category + ":"))
                .toArray(Permission[]::new);
    }
}
