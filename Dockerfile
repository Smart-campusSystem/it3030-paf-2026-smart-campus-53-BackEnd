# =============================================================================
# Smart Campus Backend — Multi-stage Docker Build
# =============================================================================
# Stage 1: Build the JAR with Maven
# Stage 2: Run with minimal JRE image
# =============================================================================

# ---------- Stage 1: Build ----------
FROM amazoncorretto:21-alpine AS builder

WORKDIR /app

# Copy Maven wrapper + POM first (layer caching for dependencies)
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Download dependencies (cached unless pom.xml changes)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build
COPY src src
RUN ./mvnw package -DskipTests -B

# ---------- Stage 2: Runtime ----------
FROM amazoncorretto:21-alpine

LABEL maintainer="Smart Campus Team"
LABEL description="Smart Campus System Backend API"

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

USER appuser

# Spring Boot default port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

# JVM tuning for containers (respects cgroup memory limits)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
