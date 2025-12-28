FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy jar (adjust name if needed)
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
