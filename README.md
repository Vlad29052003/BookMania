# Users and Authentication Microservice

The Users and Authentication Microservice is a crucial component of the BookMania online bookshop system. It is built using Java with Spring Boot, leveraging Spring Security with JWT tokens for secure authentication and authorization. Passwords are securely stored using bcrypt encryption.

## Features

- **Authentication**: Implements secure authentication using Spring Security with JWT tokens.
- **Authorization**: Ensures authorized access to resources based on user roles and permissions.
- **Integration Management**: Manages seamless integration with the Comments and Reviews Microservice and the Bookshelf Microservice.
- **Testing**: Thoroughly tested with over 95% mutation coverage and 99% branch coverage, ensuring reliability and robustness.

## Technologies Used

- Java
- Spring Boot
- Spring Security
- JWT Tokens
- Bcrypt Encryption

## Functionality

### Authentication
The microservice provides endpoints for user registration, login (which also supports two-factor authentication). Upon successful authentication, it generates a JWT token that the client can use for subsequent requests to authorized endpoints.

### Integration
It seamlessly integrates with the Comments and Reviews Microservice and the Bookshelf Microservice to provide a cohesive user experience across the entire BookMania platform.

### Password Security
Passwords are securely hashed using bcrypt before being stored in the database. This ensures that user passwords are never stored in plain text, enhancing the overall security of the system.

## Testing
The Users and Authentication Microservice has undergone extensive testing to ensure reliability and correctness. It boasts over 95% mutation coverage and 99% branch coverage, providing confidence in its functionality and performance.

## Getting Started
To get started with the Users and Authentication Microservice, follow these steps:
1. Clone the repository.
2. Configure the application properties for your database and other environment-specific settings.
3. Build and run the application using Maven or your preferred method.
4. Access the API endpoints to interact with the microservice.
