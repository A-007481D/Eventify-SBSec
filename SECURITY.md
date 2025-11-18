# Eventify - Security Implementation Guide

## 📋 Table of Contents
- [Authentication Flow](#-authentication-flow)
- [User Roles & Permissions](#-user-roles--permissions)
- [API Endpoints Security](#-api-endpoints-security)
- [Security Configuration](#-security-configuration)
- [Token Management](#-token-management)
- [Password Security](#-password-security)
- [Error Handling](#-error-handling)
- [Setup & Configuration](#-setup--configuration)
- [Testing Security](#-testing-security)
- [Pending Tasks](#-pending-tasks)
- [Security Best Practices](#-security-best-practices)

## 🔐 Authentication Flow

### User Registration
```http
POST /api/public/users
Content-Type: application/json

{
  "name": "User Name",
  "email": "user@example.com",
  "password": "securePassword123"
}
```
- Default role: `ROLE_USER`
- Password is automatically hashed using BCrypt
- Email must be unique

### User Login
```http
POST /api/public/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

### Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "role": "ROLE_USER"
}
```

## 👥 User Roles & Permissions

| Role | Description | Access Level |
|------|-------------|--------------|
| `ROLE_USER` | Regular user | Basic access to view and register for events |
| `ROLE_ORGANIZER` | Event organizer | Can create and manage events |
| `ROLE_ADMIN` | System administrator | Full access to all features |

## 🔒 API Endpoints Security

### Public Endpoints
```
POST /api/public/users
POST /api/public/login
GET  /api/public/events
```

### User Endpoints (ROLE_USER)
```
GET  /api/user/profile
POST /api/user/events/{id}/register
GET  /api/user/registrations
```

### Organizer Endpoints (ROLE_ORGANIZER)
```
POST   /api/organizer/events
PUT    /api/organizer/events/{id}
DELETE /api/organizer/events/{id}
```

### Admin Endpoints (ROLE_ADMIN)
```
GET    /api/admin/users
PUT    /api/admin/users/{id}/role
DELETE /api/admin/events/{id}
```

## ⚙️ Security Configuration

### JWT Configuration
- **Token Expiration**: 24 hours
- **Secret Key**: Stored in environment variables
- **Algorithm**: HS256

### CORS Configuration
- Allowed Origins: `*` (Update for production)
- Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
- Allowed Headers: Authorization, Content-Type

## 🔑 Token Management

### Request Format
```
Authorization: Bearer <token>
```

### Token Blacklist
- Tokens are blacklisted on logout
- Blacklist is checked on each authenticated request
- Scheduled cleanup of expired tokens

## 🔒 Password Security
- BCrypt hashing with strength 10
- Minimum password length: 6 characters
- Passwords are never stored in plain text
- Passwords are not included in API responses

## ❌ Error Handling

### Standard Error Response
```json
{
  "timestamp": "2025-11-18T18:45:42.651152508",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/admin/users"
}
```

### Common Error Codes
- `401 Unauthorized`: Invalid or missing authentication
- `403 Forbidden`: Insufficient permissions
- `400 Bad Request`: Invalid request data
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

## 🚀 Setup & Configuration

### Environment Variables
```properties
# JWT Configuration
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000  # 24 hours in milliseconds

# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/eventifydb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

### Database Schema
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER'
);
```

## 🧪 Testing Security

### Testing Authentication
```bash
# Get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/public/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}' | jq -r '.token')

# Use token
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/user/profile
```

### Testing Role-Based Access
```bash
# Test admin access
curl -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/api/admin/users

# Test organizer access
curl -H "Authorization: Bearer $ORG_TOKEN" http://localhost:8080/api/organizer/events

# Test user access (should fail)
curl -H "Authorization: Bearer $USER_TOKEN" http://localhost:8080/api/admin/users
```

## 📋 Pending Tasks

### High Priority
- [ ] Implement event management endpoints
- [ ] Add registration system
- [ ] Implement input validation
- [ ] Add rate limiting
- [ ] Implement password reset functionality

### Medium Priority
- [ ] Add audit logging
- [ ] Implement refresh tokens
- [ ] Add email verification
- [ ] Implement account locking after failed attempts

### Low Priority
- [ ] Add two-factor authentication
- [ ] Implement IP-based rate limiting
- [ ] Add security headers

## 🔒 Security Best Practices

1. **In Production**
   - Change default JWT secret
   - Configure proper CORS origins
   - Enable HTTPS
   - Use environment variables for sensitive data

2. **Development**
   - Never commit secrets to version control
   - Use `.env` files for local development
   - Regularly update dependencies

3. **Monitoring**
   - Monitor failed login attempts
   - Log security-related events
   - Set up alerts for suspicious activities

---

📅 Last Updated: November 18, 2025  
👤 Security Contact: Abdelmalek LABID
