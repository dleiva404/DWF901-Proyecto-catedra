package com.rrhh.api.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 403: el usuario está autenticado pero su rol no permite la operación. */
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException excepcion) throws IOException {
        ErroresJson.escribir(request, response, 403, "No tiene permisos para esta operación");
    }
}
