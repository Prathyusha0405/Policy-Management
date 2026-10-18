# Multi-stage Dockerfile for building and running the policy-service

# Builder stage: use Maven with JDK 21 to build the fat JAR
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy only the files needed for a build to leverage layer caching where possible
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Ensure the wrapper is executable and run the build (skip tests for faster images)
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

# Runtime stage: small JRE image
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from the builder stage (assumes a single jar in target/)
COPY --from=builder /build/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
