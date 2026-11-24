"""Resume los reportes JSON de OWASP Dependency-Check (backend y frontend) en un Markdown.

Uso:  python scripts/dc-resumen.py <etiqueta>
Copia target/dependency-check-report.{html,json} de cada modulo a reportes/dependency-check/<etiqueta>/
y genera resumen.md con las vulnerabilidades ordenadas por puntaje CVSS.
"""
import json
import pathlib
import shutil
import sys

REPO = pathlib.Path(__file__).resolve().parent.parent
MODULOS = {"backend": "Backend (pixelmeet-api)", "frontend": "Frontend (pixelmeet-web)"}
ORDEN = {"CRITICAL": 0, "HIGH": 1, "MEDIUM": 2, "MODERATE": 2, "LOW": 3, "INFO": 4}


def puntaje(v):
    for clave in ("cvssv4", "cvssv3", "cvssv2"):
        datos = v.get(clave)
        if datos:
            base = datos.get("cvssData", datos)
            score = base.get("baseScore") or datos.get("score")
            sev = base.get("baseSeverity") or datos.get("severity") or v.get("severity")
            if score is not None:
                return float(score), (sev or "").upper(), clave.upper().replace("CVSSV", "CVSS v")
    return 0.0, (v.get("severity") or "").upper(), "-"


def main():
    etiqueta = sys.argv[1]
    destino = REPO / "reportes" / "dependency-check" / etiqueta
    destino.mkdir(parents=True, exist_ok=True)
    md = [f"# OWASP Dependency-Check · {etiqueta}", ""]
    for modulo, nombre in MODULOS.items():
        for ext in ("html", "json"):
            origen = REPO / modulo / "target" / f"dependency-check-report.{ext}"
            shutil.copy(origen, destino / f"{modulo}.{ext}")
        rep = json.loads((destino / f"{modulo}.json").read_text(encoding="utf8"))
        filas = []
        deps = rep.get("dependencies", [])
        for d in deps:
            for v in d.get("vulnerabilities", []) or []:
                score, sev, version = puntaje(v)
                filas.append((score, sev, version, d.get("fileName", "?"), v.get("name", "?"),
                               (v.get("description") or "").replace("\n", " ").replace("|", "/")[:160]))
        filas.sort(key=lambda f: (-f[0], ORDEN.get(f[1], 9)))
        vulnerables = {f[3] for f in filas}
        conteo = {}
        for f in filas:
            conteo[f[1] or "?"] = conteo.get(f[1] or "?", 0) + 1
        md += [f"## {nombre}", "",
               f"Dependencias analizadas: {len(deps)} · con vulnerabilidades: {len(vulnerables)} · "
               f"vulnerabilidades: {len(filas)} · por severidad: "
               + (", ".join(f"{k} {v}" for k, v in sorted(conteo.items(), key=lambda kv: ORDEN.get(kv[0], 9)))
                  or "ninguna"), "",
               "| CVSS | Severidad | Versión | Dependencia | Vulnerabilidad | Descripción |",
               "|---|---|---|---|---|---|"]
        md += [f"| {s:.1f} | {sev} | {ver} | {dep} | {cve} | {desc} |" for s, sev, ver, dep, cve, desc in filas]
        md.append("")
    (destino / "resumen.md").write_text("\n".join(md), encoding="utf8")
    print(destino / "resumen.md")


if __name__ == "__main__":
    main()
