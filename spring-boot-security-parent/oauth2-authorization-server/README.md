# OAuth2 Authorization Server

A complete Spring Boot OAuth2 Authorization Server implementation with OpenID Connect (OIDC) support, JWT tokens, and multiple grant types.

## Features

- ✅ OAuth2 Authorization Server
- ✅ OpenID Connect (OIDC) 1.0 support
- ✅ JWT tokens with RSA-2048 key pair
- ✅ Multiple grant types:
  - Authorization Code
  - Refresh Token
  - Client Credentials
- ✅ In-memory user store with pre-configured users
- ✅ In-memory client registration with multiple clients
- ✅ H2 in-memory database with console access
- ✅ Swagger/OpenAPI documentation
- ✅ Comprehensive test coverage

## Prerequisites

- Java 21 or higher
- Maven 3.8.0 or higher
- Spring Boot 3.2.0 or higher

## Quick Start

### 1. Build the Project

```bash
mvn clean install
```

### 2. Run the Server

```bash
mvn spring-boot:run
```

Or run the application directly:

```bash
mvn exec:java -Dexec.mainClass="org.alexmond.sample.oauth2.OAuth2ServerApplication"
```

### 3. Verify Server is Running

The server will start on `http://localhost:9000`

## Pre-configured Credentials

### Users

| Username | Password | Roles |
|----------|----------|-------|
| user | password | USER |
| admin | admin | ADMIN, USER |
| client-user | client-pass | CLIENT |

### OAuth2 Clients

| Client ID | Client Secret | Grant Types | Description |
|-----------|---------------|-------------|-------------|
| oauth2-client | secret | Authorization Code, Refresh Token, Client Credentials | Web application |
| mobile-client | mobile-secret | Authorization Code, Refresh Token | Mobile application |
| service-client | service-secret | Client Credentials | Service-to-service |

## API Endpoints

### OAuth2 & OIDC Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/oauth2/authorize` | GET | Authorization endpoint |
| `/oauth2/token` | POST | Token endpoint |
| `/userinfo` | GET | User information endpoint |
| `/.well-known/openid-configuration` | GET | OIDC Discovery endpoint |
| `/.well-known/jwks.json` | GET | JSON Web Key Set endpoint |
| `/` | GET | Home/info endpoint |

### Documentation & Admin

| Endpoint | Description |
|----------|-------------|
| `/swagger-ui.html` | Swagger UI documentation |
| `/v3/api-docs` | OpenAPI specification |
| `/h2-console` | H2 Database Console |
| `/actuator` | Spring Boot Actuator endpoints |

## Testing with cURL

### 1. Get Token using Client Credentials

```bash
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "service-client:service-secret" \
  -d "grant_type=client_credentials&scope=read%20write%20admin"
```

### 2. Get User Info

```bash
curl -H "Authorization: Bearer <ACCESS_TOKEN>" \
  http://localhost:9000/userinfo
```

### 3. Get OIDC Configuration

```bash
curl http://localhost:9000/.well-known/openid-configuration
```

### 4. Get JWKS

```bash
