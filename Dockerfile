# Build stage: copy only necessary files
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY src src
COPY pom.xml pom.xml
COPY .env .env
COPY settings.xml /root/.m2/settings.xml
RUN export $(grep -v '^#' .env | xargs) && mvn clean package -s /root/.m2/settings.xml -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install CA certificates and update the trust store
RUN apk update && apk add --no-cache ca-certificates && update-ca-certificates

# If a custom MongoDB certificate is needed, copy it and update certificates
# COPY mongo-atlas.pem /usr/local/share/ca-certificates/mongo-atlas.crt
# RUN update-ca-certificates

COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/.env .env
EXPOSE 8082

# Load .env variables and run the app
ENTRYPOINT ["/bin/sh", "-c", "export $(grep -v '^#' .env | xargs) && java -jar app.jar"]
