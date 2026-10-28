
package com.rrhh.api.filter;

import com.permisos.model.Rol;
import com.permisos.model.Usuario;
import com.rrhh.api.exception.ApiError;

import javax.servlet.http.HttpServletRequest;
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
public class ApiRoleFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest request;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext context)
            throws IOException {

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

        // Los endpoints sin anotación son públicos.
        if (requiresRole == null) {
            return;
        }

        Object usuarioSesion = request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute("usuario");

        Object rolSesion = request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute("rol");

        if (!(usuarioSesion instanceof Usuario)
                || !(rolSesion instanceof Rol)) {

            abortar(context, 401, "Autenticación requerida");
            return;
        }

        Rol rol = (Rol) rolSesion;
        String nombreRol = rol.getNombre();

        if (nombreRol != null) {
            for (String rolPermitido : requiresRole.value()) {
                if (nombreRol.equalsIgnoreCase(rolPermitido)) {
                    return;
                }
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
