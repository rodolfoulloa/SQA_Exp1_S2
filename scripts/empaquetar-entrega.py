"""Genera el zip de entrega con el codigo fuente (frontend, backend, BD) y los reportes.

Uso:  python scripts/empaquetar-entrega.py [nombre.zip]   (por defecto entrega/Exp2_S4_Grupo20.zip)
Incluye los archivos versionados y los nuevos no ignorados por git; excluye entrega/, target/ y
archivos locales con secretos (.env, .sonar.env).
"""
import pathlib
import subprocess
import sys
import zipfile

REPO = pathlib.Path(__file__).resolve().parent.parent
DESTINO = REPO / (sys.argv[1] if len(sys.argv) > 1 else "entrega/Exp2_S4_Grupo20.zip")
EXCLUIR = ("entrega/",)
SECRETOS = {".env", ".sonar.env"}

archivos = subprocess.run(["git", "ls-files", "--cached", "--others", "--exclude-standard"],
                          cwd=REPO, capture_output=True, text=True, check=True).stdout.splitlines()
archivos = [a for a in archivos
            if not a.startswith(EXCLUIR) and pathlib.PurePosixPath(a).name not in SECRETOS
            and "/target/" not in a and (REPO / a).is_file()]

DESTINO.parent.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(DESTINO, "w", zipfile.ZIP_DEFLATED) as z:
    for a in sorted(archivos):
        z.write(REPO / a, f"{REPO.name}/{a}")
print(f"{DESTINO} ({len(archivos)} archivos)")
