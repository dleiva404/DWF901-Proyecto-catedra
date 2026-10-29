package com.rrhh.api.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.IOException;

@Provider
public class JsonErrorInterceptor implements ReaderInterceptor {

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context)
            throws IOException, WebApplicationException {
        try {
            return context.proceed();
        } catch (JsonProcessingException ex) {
            throw new ApiException(400,
                    "El cuerpo de la solicitud no es un JSON válido "
                            + "o tiene un formato incorrecto.");
        }
    }
}