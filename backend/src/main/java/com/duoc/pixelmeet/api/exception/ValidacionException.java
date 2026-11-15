package com.duoc.pixelmeet.api.exception;

/** Regla de negocio no cumplida sobre un campo concreto (se responde 400 con el campo). */
public class ValidacionException extends RuntimeException {

    private final String campo;

    public ValidacionException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
