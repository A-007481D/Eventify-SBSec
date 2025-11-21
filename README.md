# Eventify - Secure Event Management System

A secure event management system built with Spring Boot and Spring Security using HTTP Basic Authentication.

## 🚀 Features

- **User Authentication & Authorization**
  - HTTP Basic Authentication
  - Role-based access control (USER, ORGANIZER, ADMIN)
  - Secure password hashing with BCrypt
  - Custom AuthenticationProvider and UserDetailsService

- **Event Management**
  - Create, read, update, and delete events
  - Event registration and attendance tracking
  - Organizer dashboard

- **Admin Panel**
  - User management
  - Role management
  - Event supervision

## 🛠 Tech Stack

- **Backend**
  - Java 17
  - Spring Boot 3.x
  - Spring Security (HTTP Basic Authentication)
  - JPA/Hibernate
  - PostgreSQL
  - JUnit 5
  - Maven

## 🔒 Security Implementation

### Authentication Method
This application uses **HTTP Basic Authentication** where credentials are sent with each request in the `Authorization` header as `Basic base64(email:password)`.

### Security Features
- **CustomAuthenticationProvider**: Validates credentials using UserDetailsService and BCryptPasswordEncoder
- **CustomUserDetailsService**: Loads user details from the database
- **BCryptPasswordEncoder**: Securely hashes passwords
- **Stateless Architecture**: No server-side sessions (SessionCreationPolicy.STATELESS)
- **CSRF Disabled**: Suitable for stateless REST API
- **Custom Error Handlers**:
  - `CustomAuthenticationEntryPoint`: Handles 401 Unauthorized errors
  - `CustomAccessDeniedHandler`: Handles 403 Forbidden errors

### Role-Based Access Control

#### Roles
- `ROLE_USER`: Regular users who can view events and register
- `ROLE_ORGANIZER`: Users who can create and manage their own events
- `ROLE_ADMIN`: Administrators with full system access

#### Endpoint Protection

**Public Endpoints (No Authentication Required):**
- `POST /api/public/users` - User registration
- `GET /api/public/events` - List public events

**USER Endpoints (Requires ROLE_USER or ROLE_ADMIN):**
- `GET /api/user/profile` - Get user profile
- `POST /api/user/events/{id}/register` - Register for an event
- `GET /api/user/registrations` - Get user's registrations

**ORGANIZER Endpoints (Requires ROLE_ORGANIZER or ROLE_ADMIN):**
- `POST /api/organizer/events` - Create a new event
- `PUT /api/organizer/events/{id}` - Update own event
- `DELETE /api/organizer/events/{id}` - Delete own event

**ADMIN Endpoints (Requires ROLE_ADMIN):**
- `GET /api/admin/users` - Get all users
- `PUT /api/admin/users/{id}/role` - Update user role
- `DELETE /api/admin/events/{id}` - Delete any event

## 🚀 Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.6.3 or later
- PostgreSQL 13 or later (or use Docker Compose)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/eventify.git
   cd eventify
   ```

2. **Start PostgreSQL using Docker Compose**
   ```bash
   docker-compose up -d
   ```

3. **Configure Database (if not using Docker)**
   - Create a PostgreSQL database named `eventifydb`
   - Update `src/main/resources/application-dev.properties` with your database credentials

4. **Build the application**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

6. **Access the API**
   - API will be available at `http://localhost:8080`

## 📖 API Usage Examples

### 1. Register a New User
```bash
curl -X POST http://localhost:8080/api/public/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

### 2. Get Public Events (No Authentication)
```bash
curl -X GET http://localhost:8080/api/public/events
```

### 3. Get User Profile (Basic Auth Required)
```bash
curl -X GET http://localhost:8080/api/user/profile \
  -u john@example.com:password123
```

### 4. Register for an Event (USER role)
```bash
curl -X POST http://localhost:8080/api/user/events/1/register \
  -u john@example.com:password123
```

### 5. Create an Event (ORGANIZER role)
```bash
curl -X POST http://localhost:8080/api/organizer/events \
  -u organizer@example.com:password123 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Tech Conference 2024",
    "description": "Annual technology conference",
    "location": "Convention Center",
    "dateTime": "2024-12-15T09:00:00",
    "capacity": 500
  }'
```

### 6. Update User Role (ADMIN role)
```bash
curl -X PUT "http://localhost:8080/api/admin/users/2/role?role=ORGANIZER" \
  -u admin@example.com:password123
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Test Profile
The application includes a **test profile** that bypasses password validation for easier testing:
- Profile: `test`
- Configuration: `src/main/resources/application-test.properties`
- Uses H2 in-memory database
- Custom PasswordEncoder that accepts any password

Tests use Spring Security's `httpBasic()` method with MockMvc for authentication.

## 📝 Data Models

### User
```java
{
  "id": Long,
  "name": String,
  "email": String,
  "password": String (hashed),
  "role": String (ROLE_USER, ROLE_ORGANIZER, ROLE_ADMIN)
}
```

### Event
```java
{
  "id": Long,
  "title": String,
  "description": String,
  "location": String,
  "dateTime": LocalDateTime,
  "capacity": Integer,
  "organizerId": Long
}
```

### Registration
```java
{
  "id": Long,
  "userId": Long,
  "eventId": Long,
  "registeredAt": LocalDateTime,
  "status": String
}
```

## 🔧 Error Handling

The application includes comprehensive error handling:

### Custom Exceptions
- `UsernameAlreadyExistsException` - Email already registered
- `EventNotFoundException` - Event not found
- `UnauthorizedActionException` - User not authorized for action
- `EventCapacityExceededException` - Event at full capacity
- `UserAlreadyRegisteredException` - User already registered for event

### Standard Error Response Format
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/admin/users"
}
```

## 🤝 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📧 Contact

Your Name - [@your_twitter](https://twitter.com/your_twitter) - your.email@example.com

Project Link: [https://github.com/yourusername/eventify](https://github.com/yourusername/eventify)
