#!/usr/bin/env bash
# Genera reportes (json/md/html/pdf) del ultimo analisis de SonarCloud publicado
# para SONAR_PROJECT_KEY, consultando la Web API. No ejecuta un scan propio.
#
# Uso:
#   sonar-report.sh [--wait] [--formats json,md,html,pdf] [--output-dir DIR] [--fail-on-gate]
#
# Env requerido: SONAR_URL, SONAR_TOKEN, SONAR_PROJECT_KEY
# Env opcional:   SONAR_ORGANIZATION
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/sonar-api.sh
source "${SCRIPT_DIR}/lib/sonar-api.sh"

: "${SONAR_PROJECT_KEY:?SONAR_PROJECT_KEY es requerido}"

WAIT=false
FORMATS="json,md,html,pdf"
OUTPUT_DIR="./reports"
FAIL_ON_GATE=false

while [ $# -gt 0 ]; do
    case "$1" in
        --wait) WAIT=true; shift ;;
        --formats) FORMATS="$2"; shift 2 ;;
        --output-dir) OUTPUT_DIR="$2"; shift 2 ;;
        --fail-on-gate) FAIL_ON_GATE=true; shift ;;
        *) echo "Argumento desconocido: $1" >&2; exit 1 ;;
    esac
done

if [ "${WAIT}" = true ]; then
    "${SCRIPT_DIR}/wait-for-analysis.sh"
fi

mkdir -p "${OUTPUT_DIR}"

echo "Consultando quality gate..."
QUALITY_GATE_JSON=$(sonar_api_get "/api/qualitygates/project_status?projectKey=${SONAR_PROJECT_KEY}")

echo "Consultando metricas..."
METRIC_KEYS="bugs,vulnerabilities,code_smells,coverage,duplicated_lines_density,ncloc,security_hotspots"
MEASURES_JSON=$(sonar_api_get "/api/measures/component?component=${SONAR_PROJECT_KEY}&metricKeys=${METRIC_KEYS}")

echo "Consultando issues abiertos por severidad..."
ISSUES_JSON=$(sonar_api_get "/api/issues/search?componentKeys=${SONAR_PROJECT_KEY}&resolved=false&facets=severities&ps=1")

REPORT_JSON=$(jq -n \
    --arg projectKey "${SONAR_PROJECT_KEY}" \
    --arg organization "${SONAR_ORGANIZATION:-}" \
    --arg generatedAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
    --argjson qualityGate "$(echo "${QUALITY_GATE_JSON}" | jq '.projectStatus')" \
    --argjson measures "$(echo "${MEASURES_JSON}" | jq '[.component.measures[]? | {(.metric): .value}] | add // {}')" \
    --argjson issuesBySeverity "$(echo "${ISSUES_JSON}" | jq '[(.facets[]? | select(.property=="severities") | .values[]?) | {(.val): .count}] | add // {}')" \
    '{projectKey: $projectKey, organization: $organization, generatedAt: $generatedAt, qualityGate: $qualityGate, measures: $measures, issuesBySeverity: $issuesBySeverity}')

echo "${REPORT_JSON}" > "${OUTPUT_DIR}/sonar-report.json"
echo "Reporte JSON generado en ${OUTPUT_DIR}/sonar-report.json"

GATE_STATUS=$(echo "${REPORT_JSON}" | jq -r '.qualityGate.status')

generate_markdown() {
    local md_file="${OUTPUT_DIR}/sonar-report.md"
    {
        echo "# Reporte SonarCloud — ${SONAR_PROJECT_KEY}"
        echo ""
        echo "**Generado:** $(echo "${REPORT_JSON}" | jq -r '.generatedAt')"
        echo ""
        echo "## Quality Gate: ${GATE_STATUS}"
        echo ""
        echo "| Métrica | Comparador | Umbral | Valor actual | Estado |"
        echo "|---|---|---|---|---|"
        echo "${REPORT_JSON}" | jq -r '.qualityGate.conditions[]? | "| \(.metricKey) | \(.comparator) | \(.errorThreshold // "-") | \(.actualValue // "-") | \(.status) |"'
        echo ""
        echo "## Métricas del proyecto"
        echo ""
        echo "| Métrica | Valor |"
        echo "|---|---|"
        echo "${REPORT_JSON}" | jq -r '.measures | to_entries[] | "| \(.key) | \(.value) |"'
        echo ""
        echo "## Issues abiertos por severidad"
        echo ""
        echo "| Severidad | Cantidad |"
        echo "|---|---|"
        echo "${REPORT_JSON}" | jq -r '.issuesBySeverity | to_entries[] | "| \(.key) | \(.value) |"'
    } > "${md_file}"
    echo "Reporte Markdown generado en ${md_file}"
}

generate_html() {
    local html_file="${OUTPUT_DIR}/sonar-report.html"
    local gate_color="#2e7d32"
    [ "${GATE_STATUS}" = "ERROR" ] && gate_color="#c62828"
    [ "${GATE_STATUS}" = "WARN" ] && gate_color="#f9a825"

    {
        cat <<HTML_HEAD
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Reporte SonarCloud — ${SONAR_PROJECT_KEY}</title>
<style>
  body { font-family: Arial, sans-serif; margin: 32px; color: #1f2d3d; }
  h1 { color: #1a3a5c; }
  .gate { display: inline-block; padding: 6px 16px; border-radius: 4px; color: #fff; font-weight: bold; background: ${gate_color}; }
  table { border-collapse: collapse; width: 100%; margin: 16px 0 32px; }
  th, td { border: 1px solid #bfbfbf; padding: 8px 12px; text-align: left; font-size: 14px; }
  th { background: #1a3a5c; color: #fff; }
  tr:nth-child(even) { background: #f5f5f5; }
</style>
</head>
<body>
<h1>Reporte SonarCloud</h1>
<p><strong>Proyecto:</strong> ${SONAR_PROJECT_KEY} &nbsp; <strong>Generado:</strong> $(echo "${REPORT_JSON}" | jq -r '.generatedAt')</p>
<p><strong>Quality Gate:</strong> <span class="gate">${GATE_STATUS}</span></p>

<h2>Condiciones del Quality Gate</h2>
<table>
<tr><th>Métrica</th><th>Comparador</th><th>Umbral</th><th>Valor actual</th><th>Estado</th></tr>
HTML_HEAD
        echo "${REPORT_JSON}" | jq -r '.qualityGate.conditions[]? | "<tr><td>\(.metricKey)</td><td>\(.comparator)</td><td>\(.errorThreshold // "-")</td><td>\(.actualValue // "-")</td><td>\(.status)</td></tr>"'
        cat <<HTML_MID
</table>

<h2>Métricas del proyecto</h2>
<table>
<tr><th>Métrica</th><th>Valor</th></tr>
HTML_MID
        echo "${REPORT_JSON}" | jq -r '.measures | to_entries[] | "<tr><td>\(.key)</td><td>\(.value)</td></tr>"'
        cat <<HTML_MID2
</table>

<h2>Issues abiertos por severidad</h2>
<table>
<tr><th>Severidad</th><th>Cantidad</th></tr>
HTML_MID2
        echo "${REPORT_JSON}" | jq -r '.issuesBySeverity | to_entries[] | "<tr><td>\(.key)</td><td>\(.value)</td></tr>"'
        cat <<HTML_TAIL
</table>
</body>
</html>
HTML_TAIL
    } > "${html_file}"
    echo "Reporte HTML generado en ${html_file}"
}

generate_pdf() {
    local html_file="${OUTPUT_DIR}/sonar-report.html"
    local pdf_file="${OUTPUT_DIR}/sonar-report.pdf"

    if [ ! -f "${html_file}" ]; then
        generate_html
    fi

    if command -v xvfb-run >/dev/null 2>&1; then
        xvfb-run --auto-servernum wkhtmltopdf "${html_file}" "${pdf_file}"
    else
        wkhtmltopdf "${html_file}" "${pdf_file}"
    fi
    echo "Reporte PDF generado en ${pdf_file}"
}

IFS=',' read -ra FORMAT_LIST <<< "${FORMATS}"
for fmt in "${FORMAT_LIST[@]}"; do
    case "${fmt}" in
        json) : ;; # ya generado arriba
        md) generate_markdown ;;
        html) generate_html ;;
        pdf) generate_pdf ;;
        *) echo "Formato desconocido, se ignora: ${fmt}" >&2 ;;
    esac
done

echo ""
echo "Quality Gate: ${GATE_STATUS}"

if [ "${FAIL_ON_GATE}" = true ] && [ "${GATE_STATUS}" = "ERROR" ]; then
    echo "ERROR: el Quality Gate de SonarCloud fallo (status=ERROR)." >&2
    exit 1
fi

exit 0
