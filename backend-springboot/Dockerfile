# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build jar package
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/backend-1.0.0.jar app.jar

EXPOSE 5000

# Run JVM with garbage collection optimizations
ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "app.jar"]
