# Use the official OpenJDK image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file (Make sure it exists in the 'target' directory)
COPY target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Umgebungsvariablen setzen (Azure Auth via Service Principal)
ENV AZURE_CLIENT_ID=""
ENV AZURE_CLIENT_SECRET=""
ENV AZURE_TENANT_ID=""
ENV AZURE_KEY_VAULT_URI="https://<dein-keyvault>.vault.azure.net"
ENV AZURE_KEY_NAME="<dein-secret-name>"

# Container als ausführbare Spring Boot App starten
ENTRYPOINT ["java", "-jar", "app.jar"]
