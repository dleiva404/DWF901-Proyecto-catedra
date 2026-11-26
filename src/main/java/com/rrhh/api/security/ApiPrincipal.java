package com.rrhh.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public final class ApiPrincipal implements Principal {

    private final SesionApi sesion;

    public ApiPrincipal(SesionApi sesion) {
        this.sesion = sesion;
    }

    public static ApiPrincipal desde(SecurityContext contexto) {
        if (contexto != null) {
            Principal principal = contexto.getUserPrincipal();
            if (principal instanceof ApiPrincipal) {
                return (ApiPrincipal) principal;
            }
            // Spring Security entrega el usuario dentro de un Authentication
            if (principal instanceof Authentication
                    && ((Authentication) principal).getPrincipal() instanceof ApiPrincipal) {
                return (ApiPrincipal) ((Authentication) principal).getPrincipal();
            }
            Authentication actual = SecurityContextHolder.getContext().getAuthentication();
            if (actual != null && actual.getPrincipal() instanceof ApiPrincipal) {
                return (ApiPrincipal) actual.getPrincipal();
            }
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