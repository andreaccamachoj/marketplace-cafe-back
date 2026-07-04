#!/usr/bin/env bash
# Helpers compartidos para consultar la Web API de SonarQube/SonarCloud.
# Requiere SONAR_URL y SONAR_TOKEN en el entorno.
set -euo pipefail

: "${SONAR_URL:?SONAR_URL es requerido}"
: "${SONAR_TOKEN:?SONAR_TOKEN es requerido}"

# sonar_api_get <path>
# Ejemplo: sonar_api_get "/api/qualitygates/project_status?projectKey=foo"
sonar_api_get() {
    local path="$1"
    curl -sf -u "${SONAR_TOKEN}:" "${SONAR_URL%/}${path}"
}
