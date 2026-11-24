#!/usr/bin/env bash
# Exporta un reporte de OpenVAS a PDF (formato oficial "PDF" de Greenbone).
# Uso: herramientas/openvas/exportar-pdf.sh <report_id> <salida.pdf>
# Incluye resultados Log, Low, Medium y High, sin overrides y con QoD minimo 0, para no ocultar nada.
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
XML=$("$DIR/gmp.sh" "<get_reports report_id=\"$1\" format_id=\"c402cc3e-b531-11e1-9163-406186ea4fc5\" details=\"1\" ignore_pagination=\"1\" filter=\"apply_overrides=0 min_qod=0 levels=hmlg rows=-1 sort-reverse=severity\"/>" 2>/dev/null)
python -c "
import sys, base64, re
x = sys.stdin.read()
m = re.search(r'</report_format>([A-Za-z0-9+/=\s]+)</report>', x)
open(sys.argv[1], 'wb').write(base64.b64decode(m.group(1)))
" "$2" <<<"$XML"
echo "PDF: $2"
