# Eventify - Event Management System

[![Java CI](https://github.com/yourusername/eventify/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/yourusername/eventify/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A secure event management system built with Spring Boot and React.

## 🚀 Features

- **User Authentication & Authorization**
  - JWT-based authentication
  - Role-based access control (User, Organizer, Admin)
  - Secure password hashing with BCrypt

- **Event Management**
  - Create, read, update, and delete events
  - Event registration and attendance tracking
  - Organizer dashboard

- **Admin Panel**
  - User management
  - Role management
  - System monitoring

## 🛠 Tech Stack

- **Backend**
  - Java 17
  - Spring Boot 3.x
  - Spring Security
  - JPA/Hibernate
  - PostgreSQL
  - JUnit 5
  - Maven

- **Frontend** (Coming Soon)
  - React
  - TypeScript
  - Tailwind CSS

## 🚀 Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.6.3 or later
- PostgreSQL 13 or later
- Node.js 16+ (for frontend, when available)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/eventify.git
   cd eventify
   ```

2. **Configure Database**
   - Create a PostgreSQL database named `eventifydb`
   - Update `application-dev.properties` with your database credentials

3. **Build and Run**
   ```bash
   # Build the application
   mvn clean install
   
   # Run the application
   mvn spring-boot:run
   ```

4. **Access the API**
   - API will be available at `http://localhost:8080`
   - API documentation: `http://localhost:8080/swagger-ui.html`

## 🤝 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📧 Contact

Your Name - [@your_twitter](https://twitter.com/your_twitter) - your.email@example.com

Project Link: [https://github.com/yourusername/eventify](https://github.com/yourusername/eventify)
