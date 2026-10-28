package com.rrhh.api.filter;

import com.rrhh.api.security.ApiPrincipal;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class RegistroOperacionesFilter implements ContainerResponseFilter {

    private static final Logger LOG =
            Logger.getLogger(RegistroOperacionesFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext peticion,
                       ContainerResponseContext respuesta)
            throws IOException {

        int estado = respuesta.getStatus();

        String usuario = "anonimo";
        ApiPrincipal yo = ApiPrincipal.desde(peticion.getSecurityContext());
        if (yo != null) {
            usuario = yo.getName() + " (" + yo.getRol() + ")";
        }

        String ruta = peticion.getUriInfo().getPath();
        if (ruta.startsWith("/")) {
            ruta = ruta.substring(1);
        }

        Level nivel = Level.INFO;
        if (estado >= 500) {
            nivel = Level.SEVERE;
        } else if (estado >= 400) {
            nivel = Level.WARNING;
        }

        LOG.log(nivel, "{0} /api/{1} -> {2} usuario={3}",
                new Object[]{peticion.getMethod(), ruta, estado, usuario});
    }
}