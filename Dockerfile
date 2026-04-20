FROM eclipse-temurin:21-jre-alpine

ARG VERSION=0.0.1-SNAPSHOT

RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app

# Copy pre-built JAR from local target directory
COPY target/feed-service-${VERSION}.jar app.jar

# Copy Firebase credentials
COPY src/main/resources/config/firebase-service-account.json /app/config/firebase-service-account.json

# Create directories and set permissions
RUN mkdir -p /app/config && \
    chown -R spring:spring /app && \
    chmod 600 /app/config/firebase-service-account.json

USER spring

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
