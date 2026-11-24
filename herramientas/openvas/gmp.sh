#!/usr/bin/env bash
# Envia un comando GMP (XML) a gvmd usando el contenedor oficial gvm-tools.
# Uso: herramientas/openvas/gmp.sh '<get_feeds/>'
# La clave de admin se lee de .openvas.env (archivo local, ignorado por git).
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && (pwd -W 2>/dev/null || pwd))"
# shellcheck disable=SC1091
. "$DIR/../../.openvas.env"
MSYS_NO_PATHCONV=1 docker compose -f "$DIR/compose.yaml" run --rm -T gvm-tools \
  gvm-cli --gmp-username "$OPENVAS_USER" --gmp-password "$OPENVAS_PASSWORD" \
  socket --socketpath /run/gvmd/gvmd.sock --xml "$1"
