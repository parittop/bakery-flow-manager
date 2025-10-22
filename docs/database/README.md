# 🗄️ Database Documentation

## Overview

This section contains comprehensive documentation for the **Bakery Flow Manager** database design, including schema definitions, relationship diagrams, and optimization strategies.

## 📋 Table of Contents

- [Schema Overview](./schema.md#overview)
- [Entity Relationships](./schema.md#-er-diagram)
- [Table Definitions](./schema.md#-table-definitions)
- [Data Flow](./schema.md#-authentication-flow)
- [Performance Optimization](./schema.md#-performance-optimization)
- [Security Considerations](./schema.md#-security-considerations)
- [Migration Strategy](./schema.md#-migration-strategy)

## 🎯 Key Information

### Database Technologies
- **Primary**: PostgreSQL 14+
- **Development**: H2 (In-memory)
- **Testing**: H2 with test data
- **Migration**: Flyway (planned)

### Core Entities
- **Users** - User management and authentication
- **Roles** - Role-based access control (RBAC)
- **UserRoles** - Many-to-many relationship mapping

### Design Principles
- **Normalization**: 3NF compliance
- **Security**: Encrypted sensitive data
- **Performance**: Optimized indexes
- **Audit Trail**: Complete change tracking
- **Scalability**: Horizontal scaling ready

## 📊 Quick Reference

### Users Table
```sql
-- Primary user entity
users (id, username, email, password, profile_data, security_flags, timestamps)
```

### Roles Table
```sql
-- System roles with enum-based security
roles (id, name[ENUM], description)
```

### User Roles Table
```sql
-- Many-to-many mapping
user_roles (user_id, role_id) [COMPOSITE PK]
```

## 🔍 Common Operations

### User Authentication
```sql
-- Find user for authentication
SELECT * FROM users WHERE username = ? AND enabled = true;
```

### Role Assignment
```sql
-- Get user with roles
SELECT u.*, r.name as role_name 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id 
WHERE u.username = ?;
```

### User Statistics
```sql
-- System overview
SELECT 
    COUNT(*) as total_users,
    SUM(CASE WHEN enabled THEN 1 ELSE 0 END) as active_users,
    COUNT(DISTINCT ur.role_id) as roles_assigned
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id;
```

## 🚀 Performance Metrics

### Expected Performance
- **User Lookup**: < 10ms (indexed)
- **Authentication**: < 50ms total
- **Role Resolution**: < 5ms
- **Concurrent Users**: 1,000+ active

### Index Strategy
- Unique constraints on identifiers
- Composite indexes for common queries
- Partial indexes for boolean filters
- Functional indexes for case-insensitive search

## 🛡️ Security Highlights

### Data Protection
- **Passwords**: BCrypt hashing (strength 10)
- **PII**: Encrypted at rest
- **Audit**: Full change tracking
- **Access**: Role-based permissions

### Compliance
- **GDPR**: Right to deletion
- **SOX**: Audit trail requirements
- **ISO 27001**: Security controls

## 📈 Scaling Considerations

### Current Capacity
- **Users**: 10,000+ records
- **Roles**: 5 predefined (extensible)
- **Concurrent**: 1,000+ sessions
- **Storage**: 50GB+ with audit data

### Future Scaling
- **Read Replicas**: Report queries
- **Partitioning**: Large user bases
- **Caching**: Redis for sessions
- **Connection Pooling**: HikariCP optimization

## 🔧 Development Guidelines

### Schema Changes
1. **Migration First**: Create Flyway migration
2. **Testing**: Validate against test data
3. **Documentation**: Update diagrams
4. **Review**: Team approval required

### Query Best Practices
- Use **prepared statements** always
- **Index-aware** query design
- **Avoid N+1** problems
- **Connection management** with pool

## 📚 Related Documentation

- [API Documentation](../api/README.md) - Database-driven APIs
- [Security Configuration](../security/README.md) - Authentication flow
- [Development Setup](../development/README.md) - Local database
- [Deployment Guide](../deployment/README.md) - Production setup

## 🆘 Troubleshooting

### Common Issues
- **Connection Timeouts**: Check pool configuration
- **Slow Queries**: Review EXPLAIN plans
- **Deadlocks**: Analyze transaction order
- **Memory Issues**: Monitor heap usage

### Performance Tuning
- **Statistics**: Regular ANALYZE operations
- **Indexes**: Monitor usage patterns
- **Vacuum**: Regular maintenance
- **Monitoring**: Query performance dashboards

---

## 📞 Support

### Database Team
- **DBA**: dba@bakery.com
- **Schema Changes**: schema-team@bakery.com
- **Performance**: performance@bakery.com

### Documentation
- **Updates**: docs@bakery.com
- **Issues**: Create GitHub issue
- **Questions**: #database Slack channel

---

**Last Updated**: 2024-01-XX  
**Schema Version**: 1.0  
**Database Version**: PostgreSQL 14+

---

*For detailed schema information, see the [complete schema documentation](./schema.md)*