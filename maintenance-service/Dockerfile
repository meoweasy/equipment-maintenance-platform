# syntax=docker/dockerfile:1.7
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY platform-common/pom.xml platform-common/pom.xml
COPY equipment-service/pom.xml equipment-service/pom.xml
COPY maintenance-service/pom.xml maintenance-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl maintenance-service -am dependency:go-offline

COPY platform-common/src platform-common/src
COPY maintenance-service/src maintenance-service/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl maintenance-service -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/maintenance-service/target/maintenance-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]