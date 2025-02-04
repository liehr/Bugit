# Use the official OpenJDK image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file (Make sure it exists in the 'target' directory)
COPY target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Container als ausführbare Spring Boot App starten
ENTRYPOINT ["java", "-jar", "app.jar"]
