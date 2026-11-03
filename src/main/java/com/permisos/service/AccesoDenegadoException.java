package com.permisos.service;

public class AccesoDenegadoException extends ReglaNegocioException {

    private static final long serialVersionUID = 1L;

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}