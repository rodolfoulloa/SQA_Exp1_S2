#!/bin/sh
# Arranque del frontend. Si HTTPS esta activo y no existe un certificado, genera uno
# autofirmado (RSA 3072, SHA-256, 1 anio) con keytool y lo guarda en el volumen /certs.
# Para usar un certificado propio (por ejemplo Let's Encrypt convertido a PKCS12),
# basta con dejarlo en /certs/pixelmeet.p12 con el alias "pixelmeet".
set -eu

KEYSTORE="${SSL_KEYSTORE_PATH:-/certs/pixelmeet.p12}"

if [ "${SSL_ENABLED:-false}" = "true" ]; then
    : "${SSL_KEYSTORE_PASSWORD:?Debe definir SSL_KEYSTORE_PASSWORD}"
    if [ ! -f "$KEYSTORE" ]; then
        echo "Generando certificado autofirmado para: ${CERT_SAN:-dns:localhost,ip:127.0.0.1}"
        keytool -genkeypair -alias pixelmeet -keyalg RSA -keysize 3072 -sigalg SHA256withRSA \
            -validity 365 -storetype PKCS12 -keystore "$KEYSTORE" -storepass "$SSL_KEYSTORE_PASSWORD" \
            -dname "CN=${CERT_CN:-pixelmeet.local}, OU=ISY2202, O=Duoc UC, L=Santiago, ST=RM, C=CL" \
            -ext "SAN=${CERT_SAN:-dns:localhost,ip:127.0.0.1}" -noprompt
        chmod 600 "$KEYSTORE"
    fi
    export SSL_KEYSTORE="file:${KEYSTORE}"
fi

exec java ${JAVA_OPTS:-} -jar /app/app.jar
