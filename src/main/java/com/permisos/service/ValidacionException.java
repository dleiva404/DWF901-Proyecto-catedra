package com.permisos.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Uno o más datos recibidos son inválidos (campo faltante, formato, rango de
 * fechas, tipo inexistente...). Lleva UN texto por problema, con el formato
 * "campo: problema", el mismo que usa la API en la lista "campos" de un 400.
 *
 * La capa REST la traduce a 400. Es subclase de ReglaNegocioException para
 * que el código que ya captura ReglaNegocioException siga funcionando; por eso,
 * quien la traduzca debe comprobar ESTE tipo antes que el genérico.
 *
 * Ojo: un saldo insuficiente NO es una ValidacionException. Los datos son
 * correctos; lo que choca es el estado del saldo (el contrato lo manda a 409).
 */
public class ValidacionException extends ReglaNegocioException {

    private static final long serialVersionUID = 1L;

    private final List<String> errores;

    public ValidacionException(List<String> errores) {
        super(String.join(" ", errores));
        this.errores = Collections.unmodifiableList(new ArrayList<>(errores));
    }

    public ValidacionException(String error) {
        this(Collections.singletonList(error));
    }

    public List<String> getErrores() {
        return errores;
    }
}
