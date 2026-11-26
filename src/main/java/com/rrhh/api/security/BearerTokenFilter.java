package com.rrhh.api.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Autentica cada petición con el token "Authorization: Bearer ...".
 * No rechaza nada por sí mismo: si el token es válido deja al usuario
 * autenticado en Spring Security; si no, la cadena de seguridad responde 401
 * en las rutas que lo exigen.
 */
public class BearerTokenFilter extends OncePerRequestFilter {

    static final String ATRIBUTO_TOKEN_INVALIDO = "rrhh.token.invalido";
    private static final String ESQUEMA = "Bearer ";

    private final TokenService tokenService;

    public BearerTokenFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String cabecera = request.getHeader("Authorization");

        if (cabecera != null && !cabecera.trim().isEmpty()) {
            SesionApi sesion = tokenService.validar(extraerToken(cabecera));

            if (sesion == null) {
                request.setAttribute(ATRIBUTO_TOKEN_INVALIDO, Boolean.TRUE);
            } else {
                List<GrantedAuthority> roles = sesion.getRol() == null
                        ? Collections.<GrantedAuthority>emptyList()
                        : Collections.<GrantedAuthority>singletonList(
                                new SimpleGrantedAuthority("ROLE_" + sesion.getRol().toUpperCase()));

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                new ApiPrincipal(sesion), sesion.getToken(), roles));
            }
        }

        chain.doFilter(request, response);
    }

    private static String extraerToken(String cabecera) {
        String valor = cabecera.trim();
        if (valor.regionMatches(true, 0, ESQUEMA, 0, ESQUEMA.length())) {
            return valor.substring(ESQUEMA.length()).trim();
        }
        return null;
    }
}
