package com.rrhh.api.exception;

import com.permisos.service.ReglaNegocioException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Provider
public class ApiExceptionMapper
        implements ExceptionMapper<Exception> {

    private static final Logger LOG =
            Logger.getLogger(ApiExceptionMapper.class.getName());

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {

        int codigo = Response.Status.INTERNAL_SERVER_ERROR
                .getStatusCode();

        String mensaje = mensajeGenerico(codigo);
        List<String> campos = null;

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;

            codigo = apiException.getEstado();
            mensaje = mensajeGenerico(codigo);


            if (codigo < 500 && tieneTexto(apiException.getMessage())) {
                mensaje = apiException.getMessage();
            }
            if (exception instanceof ValidacionApiException) {
                campos = ((ValidacionApiException) exception).getCampos();
            }
        } else if (exception instanceof ReglaNegocioException) {
            codigo = 409;
            mensaje = tieneTexto(exception.getMessage())
                    ? exception.getMessage()
                    : mensajeGenerico(codigo);

        } else if (exception instanceof WebApplicationException) {
            WebApplicationException webException =
                    (WebApplicationException) exception;

            codigo = webException.getResponse().getStatus();
            mensaje = mensajeGenerico(codigo);

            if (codigo < 500 && tieneMensajePropio(webException)) {
                mensaje = webException.getMessage();
            }
        }

        String ruta = uriInfo != null
                ? uriInfo.getPath()
                : "";

        if (codigo >= 500) {
            // El detalle completo queda solo en el log, nunca en la respuesta.
            LOG.log(Level.SEVERE,
                    "Error " + codigo + " en /api/" + ruta,
                    exception);
        } else {
            LOG.log(Level.FINE,
                    "Respuesta " + codigo + " en /api/" + ruta
                            + ": " + mensaje);
        }

        ApiError error = new ApiError(codigo, mensaje, ruta, campos);

        return Response.status(codigo)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }

    private static boolean tieneTexto(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    /**
     * Cuando no se indica mensaje, JAX-RS usa un texto como
     * "HTTP 404 Not Found".
     */
    private static boolean tieneMensajePropio(
            WebApplicationException exception) {

        String mensaje = exception.getMessage();

        return mensaje != null
                && !mensaje.trim().isEmpty()
                && !mensaje.startsWith("HTTP ");
    }

    private static String mensajeGenerico(int codigo) {
        switch (codigo) {
            case 400:
                return "Solicitud incorrecta";
            case 401:
                return "Autenticación requerida";
            case 403:
                return "Acceso denegado";
            case 404:
                return "Recurso no encontrado";
            case 405:
                return "Método no permitido";
            case 406:
                return "Formato de respuesta no disponible: use application/json";
            case 409:
                return "Conflicto con el estado del recurso";
            case 415:
                return "Tipo de contenido no soportado: use application/json";
            default:
                return codigo >= 500
                        ? "Error interno del servidor"
                        : "No se pudo procesar la solicitud";
        }
    }
}