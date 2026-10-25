
package com.rrhh.api.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper
        implements ExceptionMapper<Exception> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {

        int codigo = Response.Status.INTERNAL_SERVER_ERROR
                .getStatusCode();

        String mensaje = "Error interno del servidor";

        if (exception instanceof WebApplicationException) {
            WebApplicationException webException =
                    (WebApplicationException) exception;

            codigo = webException.getResponse().getStatus();

            switch (codigo) {
                case 400:
                    mensaje = "Solicitud incorrecta";
                    break;
                case 401:
                    mensaje = "Autenticación requerida";
                    break;
                case 403:
                    mensaje = "Acceso denegado";
                    break;
                case 404:
                    mensaje = "Recurso no encontrado";
                    break;
                case 409:
                    mensaje = "Conflicto con el estado del recurso";
                    break;
                default:
                    mensaje = codigo >= 500
                            ? "Error interno del servidor"
                            : "No se pudo procesar la solicitud";
                    break;
            }
        }

        String ruta = uriInfo != null
                ? uriInfo.getPath()
                : "";

        ApiError error = new ApiError(codigo, mensaje, ruta);

        return Response.status(codigo)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
