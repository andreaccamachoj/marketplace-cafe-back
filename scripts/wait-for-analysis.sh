#!/usr/bin/env bash
# Espera a que SonarCloud tenga publicado un analisis reciente de SONAR_PROJECT_KEY.
# No dispara ni depende de un scan propio: solo verifica que el ultimo analisis
# ya visible via API (por ejemplo, el publicado por el workflow sonarcloud.yml)
# no sea demasiado antiguo, para evitar leer datos obsoletos de una corrida previa.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/sonar-api.sh
source "${SCRIPT_DIR}/lib/sonar-api.sh"

: "${SONAR_PROJECT_KEY:?SONAR_PROJECT_KEY es requerido}"

MAX_ATTEMPTS="${WAIT_MAX_ATTEMPTS:-18}"
SLEEP_SECONDS="${WAIT_SLEEP_SECONDS:-10}"
MAX_AGE_MINUTES="${WAIT_MAX_AGE_MINUTES:-15}"

echo "Esperando analisis reciente de SonarCloud para '${SONAR_PROJECT_KEY}' (maximo $((MAX_ATTEMPTS * SLEEP_SECONDS))s)..."

for attempt in $(seq 1 "${MAX_ATTEMPTS}"); do
    response=$(sonar_api_get "/api/project_analyses/search?project=${SONAR_PROJECT_KEY}&ps=1" || echo "")
    analysis_date=$(echo "${response}" | jq -r '.analyses[0].date // empty')

    if [ -n "${analysis_date}" ]; then
        analysis_epoch=$(date -d "${analysis_date}" +%s 2>/dev/null || echo 0)
        now_epoch=$(date -u +%s)
        age_minutes=$(( (now_epoch - analysis_epoch) / 60 ))

        if [ "${age_minutes}" -le "${MAX_AGE_MINUTES}" ]; then
            echo "Analisis encontrado (${analysis_date}), antiguedad ${age_minutes} min."
            exit 0
        fi
        echo "Intento ${attempt}/${MAX_ATTEMPTS}: el analisis mas reciente tiene ${age_minutes} min, esperando uno mas nuevo..."
    else
        echo "Intento ${attempt}/${MAX_ATTEMPTS}: aun sin analisis disponible..."
    fi

    sleep "${SLEEP_SECONDS}"
done

echo "ERROR: no se encontro un analisis reciente de SonarCloud tras $((MAX_ATTEMPTS * SLEEP_SECONDS))s" >&2
exit 1
