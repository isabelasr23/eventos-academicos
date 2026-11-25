FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn --batch-mode --no-transfer-progress clean verify

FROM eclipse-temurin:21-jre-noble

WORKDIR /app

COPY --from=build --chown=10001:10001 /workspace/target/eventos-academico-0.0.1-SNAPSHOT.jar ./app.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=America/Maceio", "-jar", "app.jar"]
