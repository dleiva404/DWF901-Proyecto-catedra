package com.permisos.service;

/**
 * El recurso pedido no existe o NO ES ACCESIBLE para quien lo pide.
 *
 * A propósito no se distingue "no existe" de "es de otra persona": así no se
 * revela que un identificador existe (regla del contrato de la API: los datos
 * de otro empleado no deben revelarse aunque se conozca su id).
 *
 * Es subclase de ReglaNegocioException para que el código ya existente que
 * captura ReglaNegocioException siga funcionando. La capa REST puede
 * distinguirla con instanceof y responder 404.
 */
public class RecursoNoEncontradoException extends ReglaNegocioException {

    private static final long serialVersionUID = 1L;

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
