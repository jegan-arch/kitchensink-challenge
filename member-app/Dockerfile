# ------------------------------
# Stage 1: Build the Application
# ------------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy the dependency specifications first (Cached layer)
COPY pom.xml .
# Download dependencies (offline mode to speed up future builds)
RUN mvn dependency:go-offline

# Copy the actual source code
COPY src ./src

# Build the JAR (Skip tests to speed up the build for the challenge)
RUN mvn clean package -DskipTests

# ------------------------------
# Stage 2: Run the Application
# ------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user for security (Staff Engineer Best Practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy only the compiled JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port
EXPOSE 8083

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]