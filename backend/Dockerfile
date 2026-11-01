# ---------- Etapa 1: compilacion ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---------- Etapa 2: ejecucion ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=America/Santiago
# Usuario sin privilegios para ejecutar la aplicacion
RUN useradd --system --create-home appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
