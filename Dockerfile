# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN useradd --create-home --shell /bin/bash appuser \
    && mkdir -p /app/uploads \
    && chown -R appuser:appuser /app

COPY --from=build /app/target/*.jar app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
