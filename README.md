# VK Messenger Microservices API

## Overview

This project is a microservices-based API for VK Messenger, designed with a modular architecture to provide scalable and maintainable communication features. It includes core functionalities such as messaging, feedback, publications, and user authentication.

## Features

- **Modular Microservices Architecture**: Services are independently deployed and managed.
- **Authentication & Authorization**: Integrated Keycloak for secure user authentication and role-based access control.
- **API Gateway**: Centralized entry point for routing API requests.
- **Service Discovery & Configuration**:
  - Eureka Server for service discovery.
  - Config Server for centralized configuration management.
  - Spring Boot Admin Server for monitoring microservices.
- **Core Functionalities**:
  - **Feedback Service**: Manages user comments and likes.
  - **Message Service**: Handles chat and messaging features.
  - **Messenger Service**: Orchestrates messaging-related logic.
  - **Publication Service**: Manages user publications.

## Technologies Used

- **Backend**: Java 17, Spring Boot (Spring Web, Spring Data, Spring Cloud, Spring Security)
- **Database**: PostgreSQL
- **Security**: Keycloak for authentication & authorization
- **Containerization**: Docker for service deployment
- **API Documentation**: Swagger

## Getting Started

### Prerequisites

- Java 17
- Docker & Docker Compose
- PostgreSQL
- Keycloak

### Setup Instructions

1. Clone the repository:
   ```sh
   git clone https://github.com/Doni0414/vk-spring24v1.git
   cd vk-spring24v1
   ```
2. Start the infrastructure and services using Docker Compose:
   ```sh
   docker-compose up -d
   ```
   This will start the following services:
   - **Keycloak** (Port: `8082`)
   - **Config Server** (Port: `8888`)
   - **Eureka Server** (Port: `8761`)
   - **Spring Boot Admin Server** (Port: `8090`)
   - **Publication Service**
   - **Messenger Service**
   - **Feedback Service**
   - **Message Service**
   - **API Gateway** (Port: `8086`)
3. Configure the services in `application.yml` (update database and Keycloak settings).

   ```yaml
   spring:
     cloud:
       gateway:
         routes:
           - id: publication-service-route
             uri: lb://publication-service
             predicates:
               - Host=publication.api.vk.spring24v1.${ip}.nip.io
           - id: feedback-service-route
             uri: lb://feedback-service
             predicates:
               - Host=feedback.api.vk.spring24v1.${ip}.nip.io
           - id: messenger-service-route
             uri: lb://messenger-service
             predicates:
               - Host=messenger.api.vk.spring24v1.${ip}.nip.io
           - id: message-service-route
             uri: lb://message-service
             predicates:
               - Host=message.api.vk.spring24v1.${ip}.nip.io
   ```

## API Documentation

Swagger UI provides interactive API documentation for each service:

- **Publication Service**: `http://localhost:8086/publication-api/swagger-ui.html`
- **Feedback Service**: `http://localhost:8086/feedback-api/swagger-ui.html`
- **Messenger Service**: `http://localhost:8086/messenger-api/swagger-ui.html`
- **Message Service**: `http://localhost:8086/message-api/swagger-ui.html`

## Monitoring & Administration

- **Eureka Server**: `http://localhost:8761`
- **Spring Boot Admin**: `http://localhost:8090`
