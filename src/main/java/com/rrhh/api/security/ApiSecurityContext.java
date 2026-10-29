package com.rrhh.api.security;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public final class ApiSecurityContext implements SecurityContext {

    private final ApiPrincipal principal;
    private final boolean seguro;

    public ApiSecurityContext(ApiPrincipal principal, boolean seguro) {
        this.principal = principal;
        this.seguro = seguro;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String rol) {
        return rol != null
                && principal.getRol() != null
                && principal.getRol().equalsIgnoreCase(rol);
    }

    @Override
    public boolean isSecure() {
        return seguro;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}