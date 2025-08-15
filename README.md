# Microservices Project

A multi-module Spring Boot project demonstrating microservices architecture with REST API communication between services.

## 📋 Project Overview

This project consists of two microservices:
- **User Service** (Port 8081) - Manages user data
- **Order Service** (Port 8082) - Manages orders and communicates with User Service

## 🛠️ Technology Stack

- **Java 21**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database** (In-memory)
- **Gradle** with Groovy DSL
- **Lombok**
- **Swagger UI** (OpenAPI 3)
- **Docker & Docker Compose**
- **Spring Actuator** (Health checks)

## 📁 Project Structure

```
microservices-project/
├── README.md
├── docker-compose.yml
├── .dockerignore
├── .gitignore
├── gradle/
│   └── wrapper/
├── gradlew & gradlew.bat
├── build.gradle
├── settings.gradle
├── user-service/
│   ├── Dockerfile
│   ├── build.gradle
│   └── src/main/java/com/example/userservice/
└── order-service/
    ├── Dockerfile
    ├── build.gradle
    └── src/main/java/com/example/orderservice/
```

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Docker Desktop** or **Rancher Desktop**
- **Git**

### Clone Repository

```bash
git clone <repository-url>
cd microservices-project
```

## 🐳 Running with Docker (Recommended)

### Quick Start

```bash
# Build and start all services
docker-compose up --build

# Run in background (detached mode)
docker-compose up -d --build
```

### Docker Commands

```bash
# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f user-service
docker-compose logs -f order-service

# Restart specific service
docker-compose restart user-service

# Rebuild specific service
docker-compose build user-service
docker-compose up -d user-service

# Remove containers, networks, and images
docker-compose down --rmi all --volumes
```

### Service Health Checks

Check if services are running properly:

```bash
# Check container status
docker-compose ps

# Manual health check
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Order Service
```

## 🖥️ Running Locally (Without Docker)

### Build Project

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew user-service:build
./gradlew order-service:build
```

### Start Services

**Option 1: Using Gradle**
```bash
# Terminal 1: Start User Service
./gradlew user-service:bootRun

# Terminal 2: Start Order Service  
./gradlew order-service:bootRun
```

**Option 2: Using JAR files**
```bash
# Build JAR files
./gradlew build

# Terminal 1: Start User Service
java -jar user-service/build/libs/user-service-1.0.0.jar

# Terminal 2: Start Order Service
java -jar order-service/build/libs/order-service-1.0.0.jar
```

**Option 3: IntelliJ IDEA**
1. Right-click on `UserServiceApplication` → Run
2. Right-click on `OrderServiceApplication` → Run

## 🌐 API Documentation

### Swagger UI (Interactive API Documentation)

- **User Service**: http://localhost:8081/swagger-ui.html
- **Order Service**: http://localhost:8082/swagger-ui.html

### API Endpoints

#### User Service (Port 8081)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create new user |

#### Order Service (Port 8082)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/orders` | Get all orders |
| GET | `/api/orders/{id}` | Get order by ID |
| POST | `/api/orders` | Create new order |
| GET | `/api/orders/user/{userId}` | Get user details (calls User Service) |

## 🧪 Testing with Postman

### 1. Create User

**POST** `http://localhost:8081/api/users`

```json
{
    "name": "John Doe",
    "email": "john.doe@example.com"
}
```

### 2. Get User

**GET** `http://localhost:8081/api/users/1`

### 3. Create Order (with User validation)

**POST** `http://localhost:8082/api/orders`

```json
{
    "userId": 1,
    "productName": "Laptop",
    "price": 999.99
}
```

### 4. Test Inter-service Communication

**GET** `http://localhost:8082/api/orders/user/1`

This endpoint in Order Service will call User Service internally.

## 🗄️ Database

### H2 Console Access

- **User Service H2 Console**: http://localhost:8081/h2-console
- **Order Service H2 Console**: http://localhost:8082/h2-console

**Connection Settings:**
- JDBC URL: `jdbc:h2:mem:userdb` (for User Service) or `jdbc:h2:mem:orderdb` (for Order Service)
- Username: `sa`
- Password: (leave empty)

## 🔧 Configuration

### Environment Variables (Docker)

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `USER_SERVICE_URL` | User Service URL for Order Service | `http://user-service:8081` |

### Profiles

- **Default profile**: For local development
- **Docker profile**: For Docker environment with service discovery

## 🚨 Troubleshooting

### Common Issues

**1. Port already in use**
```bash
# Check what's using the port
netstat -ano | findstr :8081
netstat -ano | findstr :8082

# Kill process (Windows)
taskkill /PID <process_id> /F
```

**2. Docker build fails**
```bash
# Clean Docker cache
docker system prune -f

# Rebuild without cache
docker-compose build --no-cache
```

**3. Services can't communicate**
```bash
# Check Docker network
docker network ls
docker network inspect microservices-project_microservices-network
```

**4. Gradle build fails**
```bash
# Clean build
./gradlew clean build

# Build with debug info
./gradlew build --info
```

### Health Check URLs

- User Service: http://localhost:8081/actuator/health
- Order Service: http://localhost:8082/actuator/health

## 📝 Development

### Adding New Endpoints

1. Create new controller method
2. Update Swagger documentation
3. Add tests
4. Rebuild Docker image if needed

### Adding New Dependencies

1. Add dependency to module's `build.gradle`
2. Refresh Gradle project
3. Rebuild Docker images

### Code Style

- Use Lombok for reducing boilerplate code
- Follow REST API naming conventions
- Add Swagger annotations for API documentation
- Include proper error handling

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

Viktor Syrovets

## 🙏 Acknowledgments

- Spring Boot team for excellent framework
- Docker team for containerization platform
- OpenAPI team for API documentation standards