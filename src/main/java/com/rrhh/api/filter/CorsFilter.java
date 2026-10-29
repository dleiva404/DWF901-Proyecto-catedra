package com.rrhh.api.filter;

import com.rrhh.api.ApiConfig;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext peticion,
                       ContainerResponseContext respuesta)
            throws IOException {

        String origen = peticion.getHeaderString("Origin");

        if (origen == null) {
            return;
        }

        MultivaluedMap<String, Object> cabeceras = respuesta.getHeaders();
        cabeceras.add("Vary", "Origin");

        List<String> permitidos = ApiConfig.origenesCors();

        if (!permitidos.contains("*") && !permitidos.contains(origen)) {
            return;
        }

        cabeceras.putSingle("Access-Control-Allow-Origin", origen);
        cabeceras.putSingle("Access-Control-Allow-Methods",
                "GET, POST, DELETE, OPTIONS");
        cabeceras.putSingle("Access-Control-Allow-Headers",
                "Authorization, Content-Type");
        cabeceras.putSingle("Access-Control-Expose-Headers",
                "Location, Content-Disposition");
        cabeceras.putSingle("Access-Control-Max-Age", "3600");
    }
}