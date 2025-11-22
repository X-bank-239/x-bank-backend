FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven

RUN mvn clean org.flywaydb:flyway-maven-plugin:11.7.2:migrate org.jooq:jooq-codegen-maven:3.19.26:generate package -DskipTests

FROM eclipse-temurin:21-jre-alpine

COPY --from=builder /app/target/x-bank-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]