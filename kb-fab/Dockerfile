# syntax=docker/dockerfile:1
# Dockerfile for Knowledge Base Fabric Subsystem (kb-fab)

FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

COPY kb-fab/pom.xml ./pom.xml
COPY kb-fab/src ./src

RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B && \
    mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

RUN addgroup -g 1001 appgroup && adduser -u 1001 -G appgroup -D appuser
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Xms512m", "-Xmx1024m", "/app/app.jar"]
