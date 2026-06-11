# syntax=docker/dockerfile:1.7

FROM --platform=$BUILDPLATFORM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn --batch-mode dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn --batch-mode package -DskipTests

FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /app

COPY --from=build /workspace/target/health-vault-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
