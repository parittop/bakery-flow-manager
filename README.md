# 🥖 Bakery Flow Manager

A comprehensive workflow management system designed specifically for bakery operations, featuring user authentication, role-based access control, and process automation.

## 🎯 Overview

Bakery Flow Manager streamlines bakery operations through:
- **User Management** with role-based permissions
- **JWT Authentication** for secure access
- **Workflow Automation** for production processes
- **Inventory Management** integration
- **Real-time Reporting** and analytics

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                 🎮 REST APIs                          │
│              (Spring Boot + Security)                 │
├─────────────────────────────────────────────────────────┤
│                 🎯 Business Logic                     │
│              (Services + Use Cases)                   │
├─────────────────────────────────────────────────────────┤
│                 🗄️ Data Access                        │
│              (Spring Data JPA)                        │
├─────────────────────────────────────────────────────────┤
│                 🏛️ Domain Models                      │
│              (User + Role Entities)                   │
└─────────────────────────────────────────────────────────┘

🔐 JWT Authentication & RBAC
📊 PostgreSQL Database
📚 Comprehensive Documentation
```

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL 14+ (production) or H2 (development)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/parittop/bakery-flow-manager.git
cd bakery-flow-manager
```

2. **Configure database**
```properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bakery_flow
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. **Run the application**
```bash
./mvnw spring-boot:run
```

4. **Access the application**
- API Base URL: http://localhost:8080
- API Documentation: http://localhost:8080/swagger-ui.html
- H2 Console (dev): http://localhost:8080/h2-console

### Default Users

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| manager | manager123 | MANAGER |
| baker | baker123 | BAKER |
| cashier | cashier123 | CASHIER |

## 📚 Documentation

### 🗄️ [Database Schema](./docs/database/schema.md)
- Entity Relationship Diagrams
- Table definitions and constraints
- Performance optimization guidelines
- Migration strategies

### 🔐 [Security Configuration](./docs/security/README.md)
- JWT Authentication implementation
- Role-based access control (RBAC)
- Security best practices

### 🎮 [API Documentation](./docs/api/README.md)
- REST API endpoints and usage
- Authentication requirements
- Error handling and responses

### 🚀 [Deployment Guide](./docs/deployment/README.md)
- Environment setup and configuration
- Docker containerization
- Production deployment steps

### 🛠️ [Development Setup](./docs/development/README.md)
- Local development environment
- Code structure and conventions
- Testing strategies

## 🎯 Features

### 🔐 Authentication & Security
- **JWT-based Authentication** with access/refresh tokens
- **Role-based Access Control** (RBAC)
- **Password Encryption** using BCrypt
- **Session Management** with configurable timeouts

### 👥 User Management
- **User Registration** with validation
- **Profile Management** with employee data
- **Role Assignment** and permissions
- **Audit Logging** for all actions

### 📊 System Features
- **RESTful API** design
- **OpenAPI/Swagger** documentation
- **Database Auditing** with timestamps
- **Configuration Management** via properties

## 🏛️ System Roles

| Role | Description | Permissions |
|------|-------------|-------------|
| **ADMIN** | System Administrator | Full system access, user management |
| **MANAGER** | Operations Manager | Manage operations, view reports |
| **BAKER** | Production Staff | Manage production workflows |
| **CASHIER** | Sales Staff | Handle orders and payments |
| **INVENTORY** | Stock Management | Manage inventory and stock |

## 🔧 Technology Stack

### Backend
- **Spring Boot 3.x** - Application framework
- **Spring Security 6.x** - Security & authentication
- **Spring Data JPA** - Database abstraction
- **PostgreSQL** - Primary database
- **JWT (jjwt)** - Token-based authentication

### Development Tools
- **Maven** - Dependency management
- **Lombok** - Code generation
- **MapStruct** - Object mapping
- **OpenAPI 3** - API documentation
- **GitGuardian** - Security scanning

### Testing
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **TestContainers** - Integration testing
- **Spring Boot Test** - Application testing

## 📊 Project Structure

```
src/main/java/com/pw/bakery/flow/
├── 🏛️ domain/          # Core business entities
│   ├── model/          # JPA entities (User, Role)
│   └── repository/     # Data access layer
├── 🎮 controller/      # REST API endpoints
├── 🎯 service/         # Business logic layer
├── 🔐 security/        # JWT authentication
├── 📦 dto/            # Data transfer objects
├── ⚙️ config/         # Spring configuration
└── 🛠️ util/          # Utility classes

docs/                   # 📚 Comprehensive documentation
├── database/          # 🗄️ Schema and data modeling
├── api/               # 🎮 API documentation
├── security/          # 🔐 Security guides
├── deployment/        # 🚀 Deployment procedures
└── development/       # 🛠️ Development setup
```

## 🔍 API Endpoints

### Authentication
```http
POST /api/auth/login     # User login
POST /api/auth/register  # User registration
POST /api/auth/refresh   # Refresh access token
POST /api/auth/logout    # User logout
```

### User Management
```http
GET    /api/users        # List users
POST   /api/users        # Create user
GET    /api/users/{id}   # Get user details
PUT    /api/users/{id}   # Update user
DELETE /api/users/{id}   # Delete user
```

### Role Management
```http
GET    /api/roles        # List roles
POST   /api/roles        # Create role
GET    /api/roles/{id}   # Get role details
PUT    /api/roles/{id}   # Update role
```

## 🛡️ Security Features

### Authentication Flow
1. User submits credentials to `/api/auth/login`
2. Server validates username/password
3. JWT tokens (access + refresh) are generated
4. Client includes access token in subsequent requests
5. Refresh token used to obtain new access tokens

### Security Measures
- **Password Hashing** with BCrypt (strength 10)
- **JWT Token Validation** with configurable expiration
- **CORS Configuration** for cross-origin requests
- **Rate Limiting** to prevent abuse
- **Security Headers** for modern browsers

## 📈 Performance & Scalability

### Performance Targets
- **API Response Time**: < 200ms (95th percentile)
- **Database Queries**: < 50ms average
- **Authentication**: < 100ms
- **System Uptime**: 99.9% availability

### Scalability Features
- **Connection Pooling** (HikariCP)
- **Database Indexing** optimization
- **Caching** support (Redis ready)
- **Horizontal Scaling** capability

## 🧪 Testing

### Test Coverage
- **Unit Tests**: Business logic validation
- **Integration Tests**: Database operations
- **Security Tests**: Authentication flows
- **API Tests**: Endpoint validation

### Running Tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthenticationServiceTest

# Run with coverage
./mvnw clean verify jacoco:report
```

## 🚀 Deployment

### Development
```bash
# Local development with H2
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### Production
```bash
# Production with PostgreSQL
./mvnw spring-boot:run -Dspring.profiles.active=prod
```

### Docker (Coming Soon)
```bash
# Build and run with Docker
docker build -t bakery-flow-manager .
docker run -p 8080:8080 bakery-flow-manager
```

## 📋 Configuration

### Environment Variables
```bash
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/bakery_flow
DB_USERNAME=bakery_user
DB_PASSWORD=secure_password

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-256-bits
JWT_ACCESS_TOKEN_EXPIRATION=1h
JWT_REFRESH_TOKEN_EXPIRATION=7d

# Application Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Code Standards
- Follow **Java Code Conventions**
- Write **unit tests** for new features
- Update **documentation** for API changes
- Ensure **GitGuardian** compliance

## 📞 Support

### Getting Help
- **Documentation**: Check the [docs](./docs/) folder
- **Issues**: [Create GitHub issue](https://github.com/parittop/bakery-flow-manager/issues)
- **Discussions**: [GitHub Discussions](https://github.com/parittop/bakery-flow-manager/discussions)

### Contact
- **Development Team**: dev-team@bakery.com
- **Support**: support@bakery.com
- **Security**: security@bakery.com

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.

## 🙏 Acknowledgments

- **Spring Team** for excellent frameworks
- **PostgreSQL Community** for robust database
- **Open Source Contributors** for valuable tools
- **Security Researchers** for vulnerability disclosures

## 🔗 Quick Links

| Section | Description | Link |
|---------|-------------|------|
| **📚 Docs** | Complete documentation | [docs/](./docs/) |
| **🗄️ Database** | Schema & data modeling | [Database Docs](./docs/database/schema.md) |
| **🎮 API** | REST endpoints | [API Docs](./docs/api/README.md) |
| **🔐 Security** | Authentication guide | [Security Docs](./docs/security/README.md) |
| **🚀 Deploy** | Production setup | [Deployment Guide](./docs/deployment/README.md) |
| **🛠️ Dev** | Local development | [Dev Guide](./docs/development/README.md) |

---

## 📊 Project Status

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Java](https://img.shields.io/badge/java-21+-orange)
![Spring Boot](https://img.shields.io/badge/spring%20boot-3.x-green)
![License](https://img.shields.io/badge/license-MIT-blue)

---

**Last Updated**: 2024-01-XX  
**Version**: 1.0.0  
**Maintainers**: Bakery Flow Manager Team

---

*🥖 **Bakery Flow Manager** - Streamlining bakery operations, one workflow at a time!*

*For questions or support, please don't hesitate to reach out to our team.* 📧