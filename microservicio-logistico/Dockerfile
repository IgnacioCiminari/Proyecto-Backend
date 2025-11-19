# 1) Build con Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# 2) Contenedor liviano con JDK 21 para ejecutar
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto de tu MS
EXPOSE 8082

# Ejecutar el JAR
ENTRYPOINT ["java", "-jar", "app.jar"]