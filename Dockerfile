# Gunakan base image Java 17
FROM openjdk:17-jdk-alpine

# Tambahkan JAR hasil build ke dalam image
COPY target/chat-0.0.1-SNAPSHOT.jar app.jar

# Jalankan aplikasi
ENTRYPOINT ["java", "-jar", "/app.jar"]
