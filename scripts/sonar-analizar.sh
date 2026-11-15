#!/usr/bin/env bash
# Analiza backend y frontend de PixelMeet con SonarQube Community.
#
# Uso:  scripts/sonar-analizar.sh <version> [raiz_del_codigo]
#   version          etiqueta del analisis en SonarQube (ej: S4-inicial, S4-final)
#   raiz_del_codigo  carpeta con backend/ y frontend/ (por defecto, la raiz del repo)
#
# Requiere SonarQube corriendo y el token en .sonar.env (SONAR_HOST_URL, SONAR_TOKEN):
#   docker run -d --name sonarqube -p 9000:9000 sonarqube:community
set -euo pipefail

VERSION="${1:?Indica la version del analisis, ej: S4-inicial}"
REPO="$(cd "$(dirname "$0")/.." && pwd)"
RAIZ="$(cd "${2:-$REPO}" && pwd)"

# shellcheck disable=SC1091
source "$REPO/.sonar.env"
export SONAR_TOKEN

analizar() {
  local modulo="$1" clave="$2" fuentes="$3"
  echo "==> Analizando $modulo como $clave ($VERSION)"
  (cd "$RAIZ/$modulo" && ./mvnw -B -q clean \
      org.jacoco:jacoco-maven-plugin:0.8.13:prepare-agent verify \
      org.jacoco:jacoco-maven-plugin:0.8.13:report \
      org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar \
      -Dsonar.host.url="$SONAR_HOST_URL" \
      -Dsonar.projectKey="$clave" \
      -Dsonar.projectVersion="$VERSION" \
      -Dsonar.sources="$fuentes" \
      -Dsonar.qualitygate.wait=false)
}

analizar backend  pixelmeet-api "pom.xml,src/main/java,src/main/resources"
analizar frontend pixelmeet-web "pom.xml,src/main/java,src/main/resources"
echo "Listo: $SONAR_HOST_URL/projects"
