package com.rrhh.api.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 401: no hay token o el token es inválido o venció. */
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException excepcion) throws IOException {
        String mensaje = request.getAttribute(BearerTokenFilter.ATRIBUTO_TOKEN_INVALIDO) != null
                ? "Token inválido o vencido"
                : "Autenticación requerida";

        response.setHeader("WWW-Authenticate", "Bearer");
        ErroresJson.escribir(request, response, 401, mensaje);
    }
}
