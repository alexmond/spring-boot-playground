# Spring Boot Form Authentication Application

A comprehensive Spring Boot application demonstrating **form-based authentication** with **Spring Security**, **application access control**, and **session management**. Users authenticate with username, password, and select an application they have access to.

## Features

- **Form-Based Authentication** - Three-parameter login (username, password, application name)
- **YAML User Configuration** - User credentials and application access lists defined in configuration
- **Application Access Control** - Validates user permissions before granting access
- **Spring Security Integration** - Custom authentication provider with BCrypt password encoding
- **Session Management** - Displays Spring Security session information on dashboard
- **Beautiful UI** - Modern, responsive login and dashboard templates with Thymeleaf
- **Logout Functionality** - Secure session termination
- **Role-Based Access** - Support for different user roles and permissions

## Project Structure

```
spring-boot-form-auth-app/
├── src/
│   ├── main/
│   │   ├── java/org/alexmond/sample/formauth/
│   │   │   ├── FormAuthApplication.java              # Main application entry point
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java              # Spring Security configuration
│   │   │   │   └── AppConfig.java                   # Application configuration
│   │   │   ├── controller/
│   │   │   │   ├── LoginController.java             # Login and authentication endpoints
│   │   │   │   └── HomeController.java              # Dashboard and session display
│   │   │   ├── service/
│   │   │   │   ├── UserService.java                 # User authentication service
│   │   │   │   ├── UserConfigService.java           # YAML configuration loading
│   │   │   │   └── ApplicationAccessService.java    # Application access validation
│   │   │   ├── model/
│   │   │   │   ├── User.java                        # User entity model
│   │   │   │   └── CustomUserDetails.java           # Spring Security UserDetails
│   │   │   └── exception/
│   │   │       └── ApplicationAccessDeniedException.java
│   │   └── resources/
│   │       ├── application.yaml                     # Spring Boot configuration
│   │       ├── users.yaml                           # User credentials and app access
│   │       └── templates/
│   │           ├── login.html                       # Login form template
│   │           └── hello.html                       # Dashboard template
│   └── test/
│       └── java/org/alexmond/sample/formauth/
│           └── FormAuthApplicationTests.java        # Unit and integration tests
└── pom.xml                                           # Maven project configuration
```

## Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- Spring Boot 3.3.0+

### Installation & Running

1. **Clone or navigate to the project directory:**
   ```bash
   cd spring-boot-security-parent/spring-boot-form-auth-app
   ```

2. **Build the project:**
   ```bash
   mvn clean install
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

   Or run the JAR:
   ```bash
   java -jar target/spring-boot-form-auth-app-1.0.0.jar
   ```

4. **Access the application:**
   Open your browser and navigate to: `http://localhost:8080`

## Demo Credentials

The application comes with pre-configured demo users in `users.yaml`:

| Username | Password  | Applications                  |
|----------|-----------|-------------------------------|
| admin    | admin123  | Banking, HR, CRM, Analytics   |
| user1    | password  | Banking, HR                   |
| user2    | test123   | CRM, Analytics                |
| guest    | guest123  | Analytics                     |

### Example Login Flow

1. Navigate to `http://localhost:8080/login`
2. Enter username: `admin`
3. Enter password: `admin123`
4. Select application: `Banking`
5. Click Login
6. View authenticated session on dashboard

## Configuration

### users.yaml

Define users and their application access:

```yaml
app:
  users:
    - username: john
      password: $2a$10/... # BCrypt hashed password
      applications:
        - Application1
        - Application2
    - username: jane
      password: $2a$10/...
      applications:
        - Application2
        - Application3
```

### Generate BCrypt Passwords

Create bcrypt hashes for passwords:

```java
// Use this to generate bcrypt hashes
String encoded = new BCryptPasswordEncoder().encode("plainPassword");
System.out.println(encoded);
```

### application.yaml

```yaml
spring:
  application:
    name: form-auth-app
  thymeleaf:
    cache: false

server:
  port: 8080

logging:
  level:
    org.springframework.security: INFO
    org.alexmond.sample.formauth: DEBUG
```

## API Endpoints

| Endpoint        | Method | Description                        |
|-----------------|--------|-----------------------------------|
| `/`             | GET    | Redirects to login                 |
| `/login`        | GET    | Display login form                 |
| `/login`        | POST   | Process login (3 parameters)       |
| `/login-error`  | GET    | Display login error page           |
| `/hello`        | GET    | Display authenticated dashboard    |
| `/logout`       | POST   | Logout and terminate session       |

## Key Features Explained

### Form Authentication with Three Parameters

- **Username**: User identifier
- **Password**: User credentials
