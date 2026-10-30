package com.rrhh.api.exception;

import com.permisos.service.ConflictoException;
import com.permisos.service.RecursoNoEncontradoException;
import com.permisos.service.ReglaNegocioException;
import com.permisos.service.ValidacionException;

/**
 * Traduce las excepciones de la capa de servicios a las de la API.
 *
 * Hace falta porque ApiExceptionMapper convierte CUALQUIER ReglaNegocioException
 * en 409, y eso daría un código equivocado para dos casos: los datos inválidos
 * (que el contrato manda a 400) y los recursos inexistentes (404).
 *
 * El orden importa: las clases específicas son subclases de ReglaNegocioException,
 * así que se comprueban primero.
 *
 *   ValidacionException          -> 400 ValidacionApiException (con la lista "campos")
 *   RecursoNoEncontradoException -> 404
 *   ConflictoException           -> 409
 *   ReglaNegocioException        -> 409 (p. ej. saldo insuficiente, según el contrato)
 *
 * Uso: {@code catch (ReglaNegocioException e) { throw TraductorErroresServicio.traducir(e); }}
 *
 * Alternativa más centralizada: agregar estos mismos casos al inicio de la rama
 * de ReglaNegocioException en ApiExceptionMapper; así ningún recurso necesitaría
 * el try/catch. Es un archivo de la parte 2, por eso aquí no se toca.
 */
public final class TraductorErroresServicio {

    private TraductorErroresServicio() {
        // clase utilitaria, no se instancia
    }

    public static ApiException traducir(ReglaNegocioException e) {
        if (e instanceof ValidacionException) {
            return new ValidacionApiException(((ValidacionException) e).getErrores());
        }
        if (e instanceof RecursoNoEncontradoException) {
            return new ApiException(404, e.getMessage());
        }
        // ConflictoException y cualquier otra regla de negocio incumplida
        return new ApiException(409, e.getMessage());
    }
}
