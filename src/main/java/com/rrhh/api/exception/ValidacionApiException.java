package com.rrhh.api.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidacionApiException extends ApiException {

    private static final long serialVersionUID = 1L;

    private final List<String> campos;

    public ValidacionApiException(List<String> campos) {
        super(400, "Hay campos inválidos.");
        this.campos = Collections.unmodifiableList(new ArrayList<>(campos));
    }

    public List<String> getCampos() {
        return campos;
    }
}