# Ganti ke openjdk 21
FROM openjdk:21-jdk-slim

# Copy JAR file hasil build Maven
COPY target/*.jar app.jar

# Jalankan aplikasi
ENTRYPOINT ["java", "-jar", "/app.jar"]
