package com.duoc.pixelmeet.api.exception;

/** Dato duplicado (correo o nombre de usuario ya en uso). */
public class ConflictoException extends RuntimeException {

    private final String campo;

    public ConflictoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
