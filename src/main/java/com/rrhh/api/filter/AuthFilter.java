package com.rrhh.api.filter;

import com.rrhh.api.exception.ApiError;
import com.rrhh.api.security.ApiPrincipal;
import com.rrhh.api.security.ApiSecurityContext;
import com.rrhh.api.security.SesionApi;
import com.rrhh.api.security.TokenService;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    private static final String ESQUEMA = "Bearer ";

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext context) throws IOException {

        if ("OPTIONS".equalsIgnoreCase(context.getMethod())) {
            return;
        }

        if (esPublico()) {
            return;
        }

        String cabecera = context.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (cabecera == null || cabecera.trim().isEmpty()) {
            rechazar(context, "Autenticación requerida");
            return;
        }

        String token = extraerToken(cabecera);
        SesionApi sesion = TokenService.getInstance().validar(token);

        if (sesion == null) {
            rechazar(context, "Token inválido o vencido");
            return;
        }

        boolean seguro = context.getSecurityContext() != null
                && context.getSecurityContext().isSecure();

        context.setSecurityContext(
                new ApiSecurityContext(new ApiPrincipal(sesion), seguro));
    }

    private boolean esPublico() {
        Method metodo = resourceInfo.getResourceMethod();
        Class<?> clase = resourceInfo.getResourceClass();

        return (metodo != null && metodo.isAnnotationPresent(Publico.class))
                || (clase != null && clase.isAnnotationPresent(Publico.class));
    }

    private static String extraerToken(String cabecera) {
        String valor = cabecera.trim();
        if (valor.regionMatches(true, 0, ESQUEMA, 0, ESQUEMA.length())) {
            return valor.substring(ESQUEMA.length()).trim();
        }
        return null;
    }

    private static void rechazar(ContainerRequestContext context,
                                 String mensaje) {
        ApiError error = new ApiError(401, mensaje,
                context.getUriInfo().getPath());

        context.abortWith(Response.status(401)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build());
    }
}