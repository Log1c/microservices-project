# Multi-stage build
# Stage 1: Build stage
FROM gradle:8-jdk21-alpine AS build

# Set working directory
WORKDIR /app

# Copy gradle files first for better caching
COPY gradle gradle
COPY gradlew gradlew.bat ./
COPY build.gradle settings.gradle ./

# Copy source code
COPY order-service order-service

# Build the application
RUN ./gradlew order-service:bootJar --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install wget for health checks
RUN apk add --no-cache wget

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Copy JAR file from build stage
COPY --from=build /app/order-service/build/libs/*.jar app.jar

# Change ownership of the app directory
RUN chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]