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
# Usuario sin privilegios; /certs guarda el keystore del certificado TLS
RUN useradd --system --create-home appuser && mkdir /certs && chown appuser:appuser /certs
COPY --from=build /app/target/*.jar app.jar
COPY --chmod=755 docker/entrypoint.sh /app/entrypoint.sh
USER appuser
VOLUME /certs
# 8080: HTTP (solo redirige a HTTPS) · 8443: HTTPS
EXPOSE 8080 8443
ENTRYPOINT ["/app/entrypoint.sh"]
