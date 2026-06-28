# syntax=docker/dockerfile:1

# ---- Build stage (full JDK 25) ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Resolve dependencies first so this layer is cached unless pom/wrapper change.
# .mvn carries the Maven wrapper (and the project-local central settings if present),
# so the build pulls straight from Maven Central — no corporate VPN needed.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Compile and package the application.
COPY src src
RUN ./mvnw -B -DskipTests clean package

# ---- Runtime stage (slim JRE 25) ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/demo-project-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
