# Bakery Flow Manager - API Documentation

## Overview

This document describes the REST API endpoints for the Bakery Flow Manager application. The API provides comprehensive user and role management capabilities with role-based access control.

## Base URL

```
http://localhost:8080/api
```

## Authentication

All API endpoints require authentication except health endpoints. The application uses Spring Security with role-based access control.

### Required Headers

```
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

### User Roles

- **ADMIN**: Full system access including user management
- **MANAGER**: Can manage operations, inventory, and view reports
- **BAKER**: Can manage production workflows and view assigned tasks
- **CASHIER**: Can handle orders and payments
- **INVENTORY**: Can manage stock and inventory

## Response Format

All API responses follow a consistent format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {},
  "errorCode": null,
  "status": "OK",
  "timestamp": "2023-12-01T10:30:00"
}
```

### Success Response Example

```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 1,
    "username": "newuser",
    "email": "newuser@example.com",
    "firstName": "New",
    "lastName": "User",
    "fullName": "New User",
    "phoneNumber": "0823456789",
    "employeeId": "NEW001",
    "enabled": true,
    "accountNonExpired": true,
    "accountNonLocked": true,
    "credentialsNonExpired": true,
    "createdAt": "2023-12-01 10:30:00",
    "updatedAt": "2023-12-01 10:30:00",
    "lastLogin": null,
    "roles": [
      {
        "id": 1,
        "name": "ADMIN",
        "displayName": "Administrator",
        "description": "Full system access including user management",
        "userCount": 1
      }
    ]
  },
  "status": "CREATED",
  "timestamp": "2023-12-01T10:30:00"
}
```

### Error Response Example

```json
{
  "success": false,
  "message": "User not found",
  "data": null,
  "errorCode": "NOT_FOUND",
  "status": "NOT_FOUND",
  "timestamp": "2023-12-01T10:30:00"
}
```

## Endpoints

### User Management

#### Get All Users

```http
GET /api/users
```

**Permissions Required:** `ADMIN`, `MANAGER`

**Query Parameters:**
- `page` (int, optional): Page number (default: 0)
- `size` (int, optional): Page size (default: 10)
- `sortBy` (string, optional): Sort field (default: "id")
- `sortDir` (string, optional): Sort direction "asc" or "desc" (default: "asc")

**Response:** Array of `UserResponse` objects

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/users?page=0&size=10&sortBy=username&sortDir=asc" \
  -H "Authorization: Bearer <token>"
```

#### Get User by ID

```http
GET /api/users/{id}
```

**Permissions Required:** `ADMIN`, `MANAGER`, or user's own account

**Path Parameters:**
- `id` (long): User ID

**Response:** `UserResponse` object

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/users/1" \
  -H "Authorization: Bearer <token>"
```

#### Get Current User

```http
GET /api/users/me
```

**Permissions Required:** Any authenticated user

**Response:** `UserResponse` object for current user

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/users/me" \
  -H "Authorization: Bearer <token>"
```

#### Create User

```http
POST /api/users
```

**Permissions Required:** `ADMIN`, `MANAGER`

**Request Body:** `UserCreateRequest`

```json
{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "firstName": "New",
  "lastName": "User",
  "phoneNumber": "0823456789",
  "employeeId": "NEW001",
  "enabled": true,
  "accountNonExpired": true,
  "accountNonLocked": true,
  "credentialsNonExpired": true,
  "roleIds": [1, 2]
}
```

**Validation Rules:**
- `username`: Required, 3-50 characters, alphanumeric and underscores only
- `email`: Required, valid email format, max 100 characters
- `password`: Required, 6-100 characters
- `firstName`: Required, max 50 characters
- `lastName`: Required, max 50 characters
- `phoneNumber`: Optional, max 20 characters, phone number format only
- `employeeId`: Required, max 20 characters
- `roleIds`: Required, at least one role ID

**Response:** Created `UserResponse` object (HTTP 201)

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/users" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123",
    "firstName": "New",
    "lastName": "User",
    "phoneNumber": "0823456789",
    "employeeId": "NEW001",
    "roleIds": [1]
  }'
```

#### Update User

```http
PUT /api/users/{id}
```

**Permissions Required:** `ADMIN`, `MANAGER`, or user's own account

**Path Parameters:**
- `id` (long): User ID

**Request Body:** `UserUpdateRequest` (all fields optional)

```json
{
  "username": "updateduser",
  "email": "updated@example.com",
  "password": "newpassword123",
  "firstName": "Updated",
  "lastName": "Name",
  "phoneNumber": "0998765432",
  "employeeId": "UPD001",
  "enabled": true,
  "accountNonExpired": true,
  "accountNonLocked": true,
  "credentialsNonExpired": true,
  "roleIds": [1, 2, 3]
}
```

**Response:** Updated `UserResponse` object

**Example Request:**
```bash
curl -X PUT "http://localhost:8080/api/users/1" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Updated",
    "lastName": "Name",
    "phoneNumber": "0998765432"
  }'
```

#### Delete User

```http
DELETE /api/users/{id}
```

**Permissions Required:** `ADMIN`

**Path Parameters:**
- `id` (long): User ID

**Response:** Success message

**Example Request:**
```bash
curl -X DELETE "http://localhost:8080/api/users/1" \
  -H "Authorization: Bearer <token>"
```

#### Search Users

```http
GET /api/users/search
```

**Permissions Required:** `ADMIN`, `MANAGER`

**Query Parameters:**
- `username` (string, optional): Partial username match
- `email` (string, optional): Partial email match
- `firstName` (string, optional): Partial first name match
- `lastName` (string, optional): Partial last name match
- `employeeId` (string, optional): Partial employee ID match
- `roleName` (string, optional): Exact role name match
- `enabled` (boolean, optional): Account enabled status

**Response:** Array of matching `UserResponse` objects

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/users/search?username=admin&enabled=true" \
  -H "Authorization: Bearer <token>"
```

#### Update User Status

```http
PATCH /api/users/{id}/status
```

**Permissions Required:** `ADMIN`, `MANAGER`

**Path Parameters:**
- `id` (long): User ID

**Query Parameters:**
- `enabled` (boolean, required): New enabled status

**Response:** Updated `UserResponse` object

**Example Request:**
```bash
curl -X PATCH "http://localhost:8080/api/users/1/status?enabled=false" \
  -H "Authorization: Bearer <token>"
```

### Role Management

#### Get All Roles

```http
GET /api/roles
```

**Permissions Required:** `ADMIN`, `MANAGER`

**Query Parameters:**
- `page` (int, optional): Page number (default: 0)
- `size` (int, optional): Page size (default: 10)
- `sortBy` (string, optional): Sort field (default: "id")
- `sortDir` (string, optional): Sort direction "asc" or "desc" (default: "asc")
- `includeUsers` (boolean, optional): Include user details (default: false)

**Response:** Array of `RoleResponse` objects

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/roles?includeUsers=true" \
  -H "Authorization: Bearer <token>"
```

#### Get Role by ID

```http
GET /api/roles/{id}
```

**Permissions Required:** `ADMIN`, `MANAGER`

**Path Parameters:**
- `id` (long): Role ID

**Query Parameters:**
- `includeUsers` (boolean, optional): Include user details (default: false)

**Response:** `RoleResponse` object

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/roles/1?includeUsers=true" \
  -H "Authorization: Bearer <token>"
```

#### Get Role by Name

```http
GET /api/roles/name/{roleName}
```

**Permissions Required:** `ADMIN`, `MANAGER`

**Path Parameters:**
- `roleName` (string): Role name (ADMIN, MANAGER, BAKER, CASHIER, INVENTORY)

**Query Parameters:**
- `includeUsers` (boolean, optional): Include user details (default: false)

**Response:** `RoleResponse` object

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/roles/name/ADMIN" \
  -H "Authorization: Bearer <token>"
```

#### Get Role Statistics

```http
GET /api/roles/statistics
```

**Permissions Required:** `ADMIN`, `MANAGER`

**Response:** Array of role statistics with user counts

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/roles/statistics" \
  -H "Authorization: Bearer <token>"
```

#### Search Roles

```http
GET /api/roles/search
```

**Permissions Required:** `ADMIN`, `MANAGER`

**Query Parameters:**
- `description` (string, required): Text to search in role descriptions
- `includeUsers` (boolean, optional): Include user details (default: false)

**Response:** Array of matching `RoleResponse` objects

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/roles/search?description=system" \
  -H "Authorization: Bearer <token>"
```

#### Create Role

```http
POST /api/roles
```

**Permissions Required:** `ADMIN`

**Query Parameters:**
- `roleName` (Role.RoleName, required): Role enum value
- `description` (string, optional): Role description

**Response:** Created `RoleResponse` object (HTTP 201)

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/roles?roleName=BAKER&description=Custom%20baker%20role" \
  -H "Authorization: Bearer <token>"
```

#### Update Role

```http
PUT /api/roles/{id}
```

**Permissions Required:** `ADMIN`

**Path Parameters:**
- `id` (long): Role ID

**Query Parameters:**
- `description` (string, required): New role description

**Response:** Updated `RoleResponse` object

**Example Request:**
```bash
curl -X PUT "http://localhost:8080/api/roles/1?description=Updated%20description" \
  -H "Authorization: Bearer <token>"
```

#### Delete Role

```http
DELETE /api/roles/{id}
```

**Permissions Required:** `ADMIN`

**Path Parameters:**
- `id` (long): Role ID

**Note:** Role can only be deleted if no users are assigned to it.

**Response:** Success message

**Example Request:**
```bash
curl -X DELETE "http://localhost:8080/api/roles/5" \
  -H "Authorization: Bearer <token>"
```

#### Get Users by Role

```http
GET /api/roles/{id}/users
```

**Permissions Required:** `ADMIN`, `MANAGER`

**Path Parameters:**
- `id` (long): Role ID

**Query Parameters:**
- `page` (int, optional): Page number (default: 0)
- `size` (int, optional): Page size (default: 10)

**Response:** Array of `UserResponse` objects assigned to the role

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/roles/1/users?page=0&size=5" \
  -H "Authorization: Bearer <token>"
```

#### Get All Role Names

```http
GET /api/roles/names
```

**Permissions Required:** `ADMIN`, `MANAGER`

**Response:** Array of all available role names

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/roles/names" \
  -H "Authorization: Bearer <token>"
```

## Data Models

### UserResponse

```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "firstName": "Admin",
  "lastName": "User",
  "fullName": "Admin User",
  "phoneNumber": "0812345678",
  "employeeId": "EMP001",
  "enabled": true,
  "accountNonExpired": true,
  "accountNonLocked": true,
  "credentialsNonExpired": true,
  "createdAt": "2023-12-01 10:30:00",
  "updatedAt": "2023-12-01 10:30:00",
  "lastLogin": null,
  "roles": [
    {
      "id": 1,
      "name": "ADMIN",
      "displayName": "Administrator",
      "description": "Full system access including user management",
      "userCount": 1
    }
  ],
  "createdBy": null,
  "updatedBy": null
}
```

### UserCreateRequest

```json
{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "firstName": "New",
  "lastName": "User",
  "phoneNumber": "0823456789",
  "employeeId": "NEW001",
  "enabled": true,
  "accountNonExpired": true,
  "accountNonLocked": true,
  "credentialsNonExpired": true,
  "roleIds": [1, 2]
}
```

### UserUpdateRequest

```json
{
  "username": "updateduser",
  "email": "updated@example.com",
  "password": "newpassword123",
  "firstName": "Updated",
  "lastName": "Name",
  "phoneNumber": "0998765432",
  "employeeId": "UPD001",
  "enabled": true,
  "accountNonExpired": true,
  "accountNonLocked": true,
  "credentialsNonExpired": true,
  "roleIds": [1, 2, 3]
}
```

### RoleResponse

```json
{
  "id": 1,
  "name": "ADMIN",
  "displayName": "Administrator",
  "description": "Full system access including user management",
  "userCount": 1,
  "users": [
    {
      "id": 1,
      "username": "admin",
      "firstName": "Admin",
      "lastName": "User",
      "fullName": "Admin User",
      "employeeId": "EMP001",
      "roles": [
        {
          "id": 1,
          "name": "ADMIN",
          "displayName": "Administrator"
        }
      ]
    }
  ]
}
```

## Error Codes

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `UNAUTHORIZED` | 401 | Authentication required or failed |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `CONFLICT` | 409 | Resource already exists or conflict |
| `INTERNAL_ERROR` | 500 | Server internal error |

## Rate Limiting

Currently, no rate limiting is implemented. However, in production, consider implementing rate limiting based on user roles and API endpoints.

## Pagination

All list endpoints support pagination with the following parameters:

- `page`: Page number (0-based)
- `size`: Number of items per page
- `sortBy`: Field to sort by
- `sortDir`: Sort direction (`asc` or `desc`)

**Response includes total count information in the message field.**

## Search

Search endpoints support partial matching and multiple criteria:

- Text searches are case-insensitive
- Multiple criteria are combined with AND logic
- Empty parameters are ignored

## Security Considerations

1. **Password Security**: All passwords are encrypted using BCrypt
2. **Input Validation**: All inputs are validated using Jakarta validation annotations
3. **Authorization**: Role-based access control is enforced on all endpoints
4. **CORS**: Cross-origin requests are allowed for development (configure appropriately for production)
5. **SQL Injection**: JPA repositories prevent SQL injection
6. **XSS Protection**: Input sanitization should be implemented in the frontend

## Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test classes
./mvnw test -Dtest=UserControllerTest,RoleControllerTest

# Run with specific profile
./mvnw test -Dspring.profiles.active=test
```

### Test Users

Default test users are automatically created:

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin123 | ADMIN |
| manager | manager123 | MANAGER |
| baker | baker123 | BAKER |
| cashier | cashier123 | CASHIER |
| inventory | inventory123 | INVENTORY |

### Example Test Scenarios

```bash
# Test getting all users as admin
curl -X GET "http://localhost:8080/api/users" \
  -H "Authorization: Basic admin:admin123"

# Test creating a user as admin
curl -X POST "http://localhost:8080/api/users" \
  -H "Authorization: Basic admin:admin123" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "employeeId": "TEST001",
    "roleIds": [1]
  }'
```

## Versioning

Current API version: v1

Future versions will be versioned using URL path (e.g., `/api/v2/users`).

## Support

For API support and issues:

1. Check the application logs for detailed error messages
2. Review the validation rules in the API documentation
3. Ensure proper authentication and authorization
4. Verify database connectivity and data integrity

---

*This documentation is for version 0.0.1-SNAPSHOT of the Bakery Flow Manager API.*