"""Exporta el resultado de SonarQube (metricas, issues y security hotspots) de los proyectos PixelMeet.

Uso:  python scripts/sonar-reporte.py <etiqueta>
Genera reportes/sonar/<etiqueta>/: un JSON por proyecto y un resumen.md legible.
La Community Edition no exporta PDF, por eso se usa su API web.
"""
import base64
import json
import pathlib
import sys
import time
import urllib.parse
import urllib.request

REPO = pathlib.Path(__file__).resolve().parent.parent
PROYECTOS = {"pixelmeet-api": "Backend (API REST)", "pixelmeet-web": "Frontend (Thymeleaf)"}
METRICAS = ("bugs,vulnerabilities,security_hotspots,code_smells,coverage,duplicated_lines_density,ncloc,"
            "reliability_rating,security_rating,sqale_rating,security_review_rating,alert_status")
SEVERIDADES = ("BLOCKER", "HIGH", "MEDIUM", "LOW", "INFO")


def cargar_env():
    env = {}
    for linea in (REPO / ".sonar.env").read_text(encoding="utf8").splitlines():
        if "=" in linea:
            k, v = linea.split("=", 1)
            env[k.strip()] = v.strip()
    return env


ENV = cargar_env()
# El token de analisis no puede leer la cola de tareas: la consulta se hace con el usuario admin local.
AUTH = "Basic " + base64.b64encode(f"{ENV['SONAR_ADMIN_USER']}:{ENV['SONAR_ADMIN_PASSWORD']}".encode()).decode()


def api(ruta, **params):
    url = f"{ENV['SONAR_HOST_URL']}{ruta}?{urllib.parse.urlencode(params)}"
    req = urllib.request.Request(url, headers={"Authorization": AUTH})
    with urllib.request.urlopen(req, timeout=30) as r:
        return json.load(r)


def paginar(ruta, clave_lista, **params):
    pagina, salida = 1, []
    while True:
        datos = api(ruta, p=pagina, ps=500, **params)
        salida += datos[clave_lista]
        total = datos.get("paging", datos.get("total", {}))
        total = total.get("total", 0) if isinstance(total, dict) else total
        if len(salida) >= total or not datos[clave_lista]:
            return salida
        pagina += 1


def esperar_tareas():
    for _ in range(60):
        if not api("/api/ce/activity", status="PENDING,IN_PROGRESS")["tasks"]:
            return
        time.sleep(3)


def severidad(issue):
    impactos = issue.get("impacts") or []
    orden = {s: i for i, s in enumerate(SEVERIDADES)}
    return min((i["severity"] for i in impactos), key=lambda s: orden.get(s, 9), default=issue.get("severity", "?"))


def main():
    etiqueta = sys.argv[1]
    destino = REPO / "reportes" / "sonar" / etiqueta
    destino.mkdir(parents=True, exist_ok=True)
    esperar_tareas()
    md = [f"# SonarQube · {etiqueta}", ""]
    for clave, nombre in PROYECTOS.items():
        medidas = {m["metric"]: m.get("value") for m in
                   api("/api/measures/component", component=clave, metricKeys=METRICAS)["component"]["measures"]}
        issues = paginar("/api/issues/search", "issues", components=clave, issueStatuses="OPEN,CONFIRMED")
        hotspots = paginar("/api/hotspots/search", "hotspots", project=clave)
        gate = api("/api/qualitygates/project_status", projectKey=clave)["projectStatus"]["status"]
        (destino / f"{clave}.json").write_text(json.dumps(
            {"proyecto": clave, "qualityGate": gate, "medidas": medidas, "issues": issues, "hotspots": hotspots},
            indent=2, ensure_ascii=False), encoding="utf8")

        md += [f"## {nombre} — `{clave}`", "",
               f"Quality Gate: **{gate}** · Líneas: {medidas.get('ncloc')} · Cobertura: {medidas.get('coverage')}% · "
               f"Duplicación: {medidas.get('duplicated_lines_density')}%", "",
               "| Métrica | Valor |", "|---|---|"]
        for m in ("vulnerabilities", "bugs", "code_smells", "security_hotspots"):
            md.append(f"| {m} | {medidas.get(m)} |")
        md += ["", "### Issues abiertos", "", "| Severidad | Tipo | Regla | Archivo:línea | Mensaje |",
               "|---|---|---|---|---|"]
        orden = {s: i for i, s in enumerate(SEVERIDADES)}
        for i in sorted(issues, key=lambda x: (orden.get(severidad(x), 9), x["component"])):
            archivo = i["component"].split(":", 1)[-1]
            md.append(f"| {severidad(i)} | {i.get('type')} | {i['rule']} | {archivo}:{i.get('line', '')} | "
                      f"{i['message'].replace('|', '/')} |")
        md += ["", "### Security Hotspots", "", "| Prioridad | Estado | Regla | Archivo:línea | Mensaje |",
               "|---|---|---|---|---|"]
        for h in hotspots:
            archivo = h["component"].split(":", 1)[-1]
            estado = h["status"] + (f" ({h['resolution']})" if h.get("resolution") else "")
            md.append(f"| {h['vulnerabilityProbability']} | {estado} | {h['ruleKey']} | {archivo}:{h.get('line', '')} | "
                      f"{h['message'].replace('|', '/')} |")
        md.append("")
    (destino / "resumen.md").write_text("\n".join(md), encoding="utf8")
    print(destino / "resumen.md")


if __name__ == "__main__":
    main()
