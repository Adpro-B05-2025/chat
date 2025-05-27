# Gunakan image OpenJDK 21 yang ringan
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Salin JAR hasil build dari Gradle ke container
COPY build/libs/*.jar app.jar

# Ekspose port 8080 (Spring Boot default)
EXPOSE 8080

# Jalankan aplikasi
ENTRYPOINT ["java", "-jar", "app.jar"]