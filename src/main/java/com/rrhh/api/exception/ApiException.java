package com.rrhh.api.exception;

public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int estado;

    public ApiException(int estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public int getEstado() {
        return estado;
    }
}