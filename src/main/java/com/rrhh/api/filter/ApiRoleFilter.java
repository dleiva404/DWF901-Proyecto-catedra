
package com.rrhh.api.filter;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import com.rrhh.api.exception.ApiError;

import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class ApiRoleFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext context)
            throws IOException {

        if ("OPTIONS".equalsIgnoreCase(context.getMethod())) {
            return;
        }

        RequiresRole requiresRole = null;

        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();

        if (method != null) {
            requiresRole = method.getAnnotation(RequiresRole.class);
        }

        if (requiresRole == null && resourceClass != null) {
            requiresRole =
                    resourceClass.getAnnotation(RequiresRole.class);
        }

        if (requiresRole == null) {
            return;
        }

        SecurityContext seguridad = context.getSecurityContext();

        if (seguridad == null || seguridad.getUserPrincipal() == null) {
            abortar(context, 401, "Autenticación requerida");
            return;
        }

        for (String rolPermitido : requiresRole.value()) {
            if (seguridad.isUserInRole(rolPermitido)) {
                return;
            }
        }

        abortar(context, 403, "No tiene permisos para esta operación");
    }

    private void abortar(
            ContainerRequestContext context,
            int codigo,
            String mensaje) {

        String ruta = context.getUriInfo().getPath();

        ApiError error = new ApiError(codigo, mensaje, ruta);

        context.abortWith(
                Response.status(codigo)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(error)
                        .build()
        );
    }
}
