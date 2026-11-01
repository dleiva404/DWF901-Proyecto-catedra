package com.permisos.service;

/**
 * La operación choca con el estado actual del recurso (por ejemplo, cancelar
 * una solicitud que ya no está pendiente). La capa REST la traduce a 409.
 *
 * Es subclase de ReglaNegocioException para que el código ya existente que
 * captura ReglaNegocioException siga funcionando.
 */
public class ConflictoException extends ReglaNegocioException {

    private static final long serialVersionUID = 1L;

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
