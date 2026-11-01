#!/usr/bin/env bash
# Opcional. Crea .env con la IP de la VM en el certificado y una clave JWT fija.
# Uso: ./scripts/generar-env.sh [IP_PUBLICA]
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
DB_PASSWORD=game2026
DB_ROOT_PASSWORD=root2026
JWT_SECRET=$(aleatorio 48)
JWT_EXPIRATION_MINUTES=60
SSL_KEYSTORE_PASSWORD=cert2026
CERT_CN=${IP:-pixelmeet.local}
CERT_SAN=$SAN
SEED_ADMIN_PASSWORD=Admin123!
SEED_GAMER_PASSWORD=Gamer123!
EOT
echo ".env creado (certificado para: $SAN)"
