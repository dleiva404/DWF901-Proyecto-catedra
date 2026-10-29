package com.rrhh.api.validation;

import com.rrhh.api.exception.ValidacionApiException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidadorApi {

    private final List<String> errores = new ArrayList<>();

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    public ValidadorApi obligatorio(String campo, String valor) {
        if (vacio(valor)) {
            errores.add(campo + ": es obligatorio");
        }
        return this;
    }

    public ValidadorApi longitud(String campo, String valor,
                                 int minimo, int maximo) {
        if (!vacio(valor)) {
            int largo = valor.trim().length();
            if (largo < minimo || largo > maximo) {
                errores.add(campo + ": debe tener entre " + minimo
                        + " y " + maximo + " caracteres");
            }
        }
        return this;
    }

    public LocalDate fecha(String campo, String valor) {
        if (vacio(valor)) {
            errores.add(campo + ": es obligatoria");
            return null;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException ex) {
            errores.add(campo + ": debe tener el formato yyyy-MM-dd");
            return null;
        }
    }

    public ValidadorApi positivo(String campo, Integer valor) {
        if (valor == null || valor <= 0) {
            errores.add(campo + ": debe ser un entero mayor que cero");
        }
        return this;
    }

    public ValidadorApi error(String campo, String problema) {
        errores.add(campo + ": " + problema);
        return this;
    }

    public boolean hayErrores() {
        return !errores.isEmpty();
    }

    public List<String> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    public void lanzarSiHayErrores() {
        if (!errores.isEmpty()) {
            throw new ValidacionApiException(errores);
        }
    }
}