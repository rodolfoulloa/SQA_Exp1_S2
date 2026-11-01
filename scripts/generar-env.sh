#!/usr/bin/env bash
# Crea el archivo .env con claves aleatorias. Uso: ./scripts/generar-env.sh [IP_PUBLICA]
set -euo pipefail
cd "$(dirname "$0")/.."

if [ -f .env ]; then
    echo ".env ya existe; no se sobrescribe." >&2
    exit 1
fi

IP="${1:-$(curl -s --max-time 5 https://api.ipify.org || true)}"
SAN="dns:localhost,ip:127.0.0.1"
if [ -n "$IP" ] && [ "$IP" != "127.0.0.1" ]; then
    SAN="$SAN,ip:$IP"
fi

aleatorio() { openssl rand -base64 "$1" | tr -d '\n'; }

umask 077
cat > .env <<EOT
DB_USER=pixelmeet
DB_PASSWORD=$(aleatorio 24 | tr -d '/+=')
DB_ROOT_PASSWORD=$(aleatorio 24 | tr -d '/+=')
JWT_SECRET=$(aleatorio 48)
JWT_EXPIRATION_MINUTES=60
SSL_KEYSTORE_PASSWORD=$(aleatorio 24 | tr -d '/+=')
CERT_CN=${IP:-pixelmeet.local}
CERT_SAN=$SAN
SEED_ADMIN_PASSWORD=Admin123!
SEED_GAMER_PASSWORD=Gamer123!
EOT
echo ".env creado (certificado para: $SAN)"
