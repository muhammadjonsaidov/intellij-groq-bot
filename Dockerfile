FROM maven:3.9.7-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn -Dmaven.wagon.http.ssl.insecure=true dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p /app/temp_audio
VOLUME /app/temp_audio

ENTRYPOINT ["java", "-jar", "app.jar"]