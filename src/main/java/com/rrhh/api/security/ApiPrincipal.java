package com.rrhh.api.security;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public final class ApiPrincipal implements Principal {

    private final SesionApi sesion;

    public ApiPrincipal(SesionApi sesion) {
        this.sesion = sesion;
    }

    public static ApiPrincipal desde(SecurityContext contexto) {
        if (contexto != null
                && contexto.getUserPrincipal() instanceof ApiPrincipal) {
            return (ApiPrincipal) contexto.getUserPrincipal();
        }
        return null;
    }

    @Override
    public String getName() { return sesion.getUsername(); }

    public int getUsuarioId() { return sesion.getUsuarioId(); }

    public int getEmpleadoId() { return sesion.getEmpleadoId(); }

    public String getNombre() { return sesion.getNombre(); }

    public String getRol() { return sesion.getRol(); }

    public String getToken() { return sesion.getToken(); }
}