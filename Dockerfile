FROM maven:3.9.11-eclipse-temurin-17-alpine AS builder

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests


# Stage 2: Runtime Stage

FROM tomcat:9.0.108-jdk17-temurin

LABEL maintainer="Ratnesh Vansh"

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD [ "catalina.sh", "run" ]
