FROM openjdk:17-jdk-slim AS build

WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw package

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/demo-project-*.jar demo-project.jar
ENTRYPOINT ["java", "-jar", "demo-project.jar"]
