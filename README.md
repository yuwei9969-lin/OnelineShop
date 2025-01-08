# Store Application - Enterprise Online Store System

## Overview
Store Application is a robust enterprise-scale online store application built with Spring Boot that enables customers to purchase physical items from multiple warehouses. The system integrates with various external services including Bank, DeliveryCo, and EmailService to provide a complete e-commerce solution.

## System Architecture

### Core Components
- **User Management**: Handles user authentication and bank account linking
- **Order Processing**: Manages the complete order lifecycle
- **Warehouse Management**: Coordinates stock checks and inventory management
- **Payment Processing**: Integrates with banking services
- **Delivery Management**: Coordinates with delivery services
- **Email Notification**: Sends status updates to customers

### External Service Integration
- Bank Service: Handles payment processing and refunds
- DeliveryCo: Manages product delivery and shipping status updates
- EmailService: Sends notifications to customers

### Technology Stack
- Backend: Spring Boot
- Database: JPA/Hibernate
- Message Queue: RabbitMQ
- Security: JWT Authentication
- API: RESTful Web Services

## Features

### User Management
- User registration and authentication
- JWT-based session management
- Bank account linking
- Secure password hashing using BCrypt

### Order Management
- Order creation and processing
- Real-time stock verification
- Multi-warehouse fulfillment
- Order status tracking
- Order cancellation with automatic refund

### Payment Processing
- Secure payment handling
- Automated refund processing
- Transaction status tracking

### Delivery Management
- Real-time delivery status updates
- Multi-warehouse pickup coordination
- Delivery status notifications

### Email Notifications
- Order confirmation
- Payment status updates
- Delivery status notifications
- Order cancellation confirmations

## API Documentation

### User Management APIs

#### 1. Register User
- **Endpoint**: POST /api/users/register
- **Description**: Register a new user
- **Request Body**:
```json
{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
}
```
- **Response**: Returns UserDTO with user information

#### 2. User Login
- **Endpoint**: POST /api/users/login
- **Description**: Authenticate user and get JWT token
- **Request Body**:
```json
{
    "username": "testuser",
    "password": "password123"
}
```
- **Response**: Returns JWT token and username

#### 3. Link Bank Account
- **Endpoint**: POST /api/users/{userId}/link-bank-account
- **Description**: Link a bank account to user's profile
- **Request Body**:
```json
{
    "bankCustomerId": 1,
    "bankAccountId": 1
}
```
- **Response**: Returns updated UserDTO

### Order Management APIs

#### 1. Create Order
- **Endpoint**: POST /api/orders
- **Description**: Create a new order
- **Request Body**:
```json
{
    "username": "testuser",
    "productId": 1,
    "quantity": 2
}
```
- **Response**: Returns order creation confirmation message

#### 2. Get Order
- **Endpoint**: GET /api/orders/{orderId}
- **Description**: Retrieve order details
- **Response**: Returns order details or 404 if not found

#### 3. Cancel Order
- **Endpoint**: PUT /api/orders/{orderId}/cancel
- **Description**: Cancel an existing order
- **Response**: Returns cancellation confirmation or error message

## Message Queue Structure

### Exchanges
- store.order.request.exchange: Handles order-related requests
- store.order.response.exchange: Handles service responses
- store.email.request.exchange: Handles email notifications

### Key Queues
- Warehouse: Request/Response queues for stock management
- Payment: Request/Response queues for payment processing
- Delivery: Request/Response queues for delivery management
- Email: Request queue for email notifications
- Refund: Request/Response queues for refund processing

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven
- RabbitMQ Server
- PostgreSQL Database

### Configuration
1. Configure database connection in application.properties
2. Set up RabbitMQ connection details
3. Configure external service endpoints
4. Set JWT secret and expiration time

### Building and Running
```bash
# Clone the repository
git clone [repository-url]

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Default User Account
- Username: customer
- Password: COMP5348

## System Features

### Fault Tolerance and Reliability
- Asynchronous message processing
- Transaction management for data consistency
- Automatic retry mechanisms
- Failed payment handling
- Order cancellation safety measures
- Stock reservation system

### Monitoring and Logging
- Comprehensive logging system using Java Logging
- Transaction tracking
- Error handling and reporting
- Service health monitoring

### Security Measures
- Password encryption using BCrypt
- JWT-based authentication
- Secure communication channels
- Input validation and sanitization

### Performance Considerations
- Asynchronous processing for long-running operations
- Message queue for load handling
- Database indexing for quick lookups
- Connection pooling

## Contributing
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License
This project is part of COMP5348 Enterprise-Scale Software Development coursework.
