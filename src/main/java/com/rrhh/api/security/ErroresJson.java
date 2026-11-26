package com.rrhh.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.api.exception.ApiError;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Escribe los errores de seguridad con el mismo JSON que el resto de la API. */
final class ErroresJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ErroresJson() {
    }

    static void escribir(HttpServletRequest request, HttpServletResponse response,
                         int codigo, String mensaje) throws IOException {
        ApiError error = new ApiError(codigo, mensaje, request.getPathInfo());

        response.setStatus(codigo);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(response.getWriter(), error);
    }
}
