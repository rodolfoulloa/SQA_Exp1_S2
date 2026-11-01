package com.duoc.pixelmeet.web.client;

import java.util.Map;

/** Error devuelto por la API o problema de comunicacion con ella. */
public class ApiException extends RuntimeException {

    public enum Tipo {
        /** 400 o 409: datos invalidos o duplicados. */
        VALIDACION,
        /** 401: credenciales invalidas o token vencido. */
        NO_AUTENTICADO,
        /** 403: el rol no tiene permiso. */
        PROHIBIDO,
        /** 404. */
        NO_ENCONTRADO,
        /** 429: demasiados intentos de login. */
        DEMASIADOS_INTENTOS,
        /** La API no responde o devolvio 5xx. */
        NO_DISPONIBLE
    }

    private final Tipo tipo;
    private final Map<String, String> campos;

    public ApiException(Tipo tipo, String mensaje, Map<String, String> campos) {
        super(mensaje);
        this.tipo = tipo;
        this.campos = campos == null ? Map.of() : Map.copyOf(campos);
    }

    public ApiException(Tipo tipo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.tipo = tipo;
        this.campos = Map.of();
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}
