# Database Setup Guide

This document explains the database configuration and setup for Bakery Flow Manager.

## 🏗️ Database Architecture

### Supported Databases

1. **H2 Database** (Development & Testing)
   - In-memory for testing (`application.properties`)
   - File-based for development (`application-dev.properties`)
   - No external setup required

2. **PostgreSQL** (Production)
   - Persistent storage for production environment
   - Requires external PostgreSQL server

## 📁 Database Files Structure

```
src/main/resources/
├── application.properties          # Default config (test environment)
├── application-dev.properties      # Development config (H2 file)
├── application-prod.properties     # Production config (PostgreSQL)
├── data-dev.sql                   # Development data initialization
├── data-prod.sql                  # Production data initialization
└── db/migration/
    └── V1__Create_User_Role_Tables.sql  # Flyway migration script
```

## 🔧 Environment Configurations

### Development Environment (`dev` profile)

**Database Type:** H2 File-based
```properties
spring.datasource.url=jdbc:h2:file:./data/bakery-dev;AUTO_SERVER=TRUE
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

**Features:**
- Data persists between application restarts
- H2 Console available at `/h2-console`
- Automatic schema updates
- Comprehensive test data

**Default Users:**
- Username: `admin`, Password: `admin123`
- Username: `manager`, Password: `manager123`
- Username: `baker`, Password: `baker123`
- Username: `cashier`, Password: `cashier123`
- Username: `inventory`, Password: `inventory123`

### Production Environment (`prod` profile)

**Database Type:** PostgreSQL
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bakery_prod
spring.datasource.username=bakery_user
spring.datasource.password=${BAKERY_DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
```

**Features:**
- Production-ready PostgreSQL database
- Connection pooling with HikariCP
- Schema validation (no auto-creation)
- SSL support
- Comprehensive logging
- Health checks and metrics

### Test Environment (`test` profile)

**Database Type:** H2 In-memory
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=false
```

**Features:**
- In-memory database for fast testing
- Clean state for each test run
- Minimal logging

## 🚀 Getting Started

### 1. Development Setup

```bash
# Run with development profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or set as default in application.properties
spring.profiles.active=dev
```

### 2. Production Setup

**Step 1: Install PostgreSQL**
```bash
# Ubuntu/Debian
sudo apt-get install postgresql postgresql-contrib

# macOS (with Homebrew)
brew install postgresql
brew services start postgresql

# Windows
# Download and install from https://postgresql.org/download/windows/
```

**Step 2: Create Database**
```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database and user
CREATE DATABASE bakery_prod;
CREATE USER bakery_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE bakery_prod TO bakery_user;
\q
```

**Step 3: Configure Environment Variables**
```bash
export BAKERY_DB_PASSWORD=your_secure_password
export SSL_KEYSTORE_PASSWORD=your_keystore_password
```

**Step 4: Run Application**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### 3. Database Migrations

The application uses Flyway for database migrations:

```bash
# Run migrations manually
./mvnw flyway:migrate

# Check migration status
./mvnw flyway:info

# Validate current state
./mvnw flyway:validate
```

## 📊 Database Schema

### Core Tables

#### `roles`
Stores user roles and permissions.
```sql
CREATE TABLE roles (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

#### `users`
Stores user accounts and authentication data.
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    employee_id VARCHAR(20) UNIQUE,
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    last_login TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);
```

#### `user_roles`
Many-to-many relationship between users and roles.
```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
```

## 🔐 Security Configuration

### Password Encryption
- Uses BCrypt with strength 10
- All passwords are encrypted before storage
- Default passwords should be changed in production

### Default Roles
1. **ADMIN** - Full system access
2. **MANAGER** - Operations and inventory management
3. **BAKER** - Production workflow management
4. **CASHIER** - Order and payment processing
5. **INVENTORY** - Stock management

## 🛠️ Maintenance

### Database Backups (PostgreSQL)
```bash
# Create backup
pg_dump -h localhost -U bakery_user bakery_prod > backup_$(date +%Y%m%d).sql

# Restore backup
psql -h localhost -U bakery_user bakery_prod < backup_20231201.sql
```

### Monitoring
- Application logs: `logs/bakery-flow-manager.log`
- Database metrics available via Actuator endpoints
- Health checks at `/actuator/health`

## 🧪 Testing

### Unit Tests
```bash
# Run repository tests
./mvnw test -Dtest=RoleRepositoryTest,UserRepositoryTest

# Run data initializer tests
./mvnw test -Dtest=DataInitializerTest
```

### Integration Tests
```bash
# Run all tests
./mvnw test

# Run tests with specific profile
./mvnw test -Dspring.profiles.active=test
```

## 📝 Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `BAKERY_DB_PASSWORD` | PostgreSQL password | - | Yes (prod) |
| `SSL_KEYSTORE_PASSWORD` | SSL keystore password | - | Yes (prod) |
| `SPRING_PROFILES_ACTIVE` | Active profile | `test` | No |

## 🔍 Troubleshooting

### Common Issues

1. **H2 Console Access Denied**
   - Ensure `spring.h2.console.enabled=true` in dev profile
   - Check browser URL: `http://localhost:8080/h2-console`

2. **PostgreSQL Connection Failed**
   - Verify PostgreSQL service is running
   - Check database credentials
   - Ensure database exists

3. **Migration Failures**
   - Check Flyway schema history table
   - Validate SQL syntax in migration files
   - Use `./mvnw flyway:repair` if needed

4. **Data Initialization Issues**
   - Check application logs for error messages
   - Verify role creation before user creation
   - Ensure password encoder is configured

### Debug Mode

Enable debug logging:
```properties
logging.level.com.pw.bakery.flow=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

## 📚 Additional Resources

- [Spring Boot Database Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html)
- [H2 Database Documentation](http://www.h2database.com/html/cheatSheet.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
```

## 🎉 ทดสอบการทำงาน

ตอนนี้ทดสอบรัน application ด้วย development profile:
