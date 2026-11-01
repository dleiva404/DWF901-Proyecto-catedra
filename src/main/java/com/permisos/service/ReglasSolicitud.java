package com.permisos.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Validaciones de los datos de una solicitud y de una incapacidad que llegan
 * por la API REST, con los límites del CONTRATO (docs/contrato-api-rest.md).
 *
 * Es Java puro: no usa JPA, Servlets ni JAX-RS, así que se prueba sin levantar nada.
 *
 * Cada problema se reporta como "campo: problema" (el mismo formato de la lista
 * "campos" de un 400) y se reúnen TODOS los del mismo envío en una
 * ValidacionException, para que quien consume la API los corrija de una vez.
 *
 * Nota: el formulario web tiene sus propios validadores JSF (motivo hasta 500
 * caracteres). La API sigue el contrato: motivo de 5 a 250.
 */
public final class ReglasSolicitud {

    public static final int MOTIVO_MIN = 5;
    public static final int MOTIVO_MAX = 250;
    public static final int DOCUMENTO_MAX = 100;
    public static final int OBSERVACIONES_MAX = 500;

    static final String ERROR_SOLO_DOMINGOS =
            "fechaFin: el rango solo incluye domingos; elija al menos un día de lunes a sábado";

    private ReglasSolicitud() {
        // clase utilitaria, no se instancia
    }

    /**
     * Valida los datos de una solicitud NUEVA (vacaciones, ausencia...) y
     * devuelve los días que se le contarán (de lunes a sábado).
     *
     * @param hoy fecha de referencia para rechazar fechas pasadas (parámetro
     *            para poder probarlo con fechas fijas)
     * @throws ValidacionException con todos los errores encontrados
     */
    public static int validarNuevaSolicitud(Integer idTipo, String motivo,
                                            LocalDate inicio, LocalDate fin, LocalDate hoy)
            throws ValidacionException {

        List<String> errores = new ArrayList<>();

        if (idTipo == null) {
            errores.add("tipoSolicitudId: es obligatorio");
        } else if (idTipo <= 0) {
            errores.add("tipoSolicitudId: debe ser un entero mayor que cero");
        }

        validarMotivo(motivo, errores);

        int dias = validarRangoDeFechas(inicio, fin, errores, hoy);

        lanzarSiHayErrores(errores);
        return dias;
    }

    /**
     * Valida los datos de una incapacidad y devuelve los días que se le
     * contarán (de lunes a sábado).
     *
     * A diferencia de una solicitud normal, una incapacidad PUEDE empezar en el
     * pasado: la persona se incapacita y la registra después.
     */
    public static int validarIncapacidad(String numeroDocumento, String observaciones,
                                         LocalDate inicio, LocalDate fin)
            throws ValidacionException {

        List<String> errores = new ArrayList<>();

        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            errores.add("numeroDocumento: es obligatorio");
        } else if (numeroDocumento.trim().length() > DOCUMENTO_MAX) {
            errores.add("numeroDocumento: debe tener como máximo " + DOCUMENTO_MAX + " caracteres");
        }

        if (observaciones != null && observaciones.trim().length() > OBSERVACIONES_MAX) {
            errores.add("observaciones: debe tener como máximo " + OBSERVACIONES_MAX + " caracteres");
        }

        int dias = validarRangoDeFechas(inicio, fin, errores, null);

        lanzarSiHayErrores(errores);
        return dias;
    }

    /**
     * @param hoy si no es null, se rechaza un inicio anterior a esa fecha
     * @return los días de lunes a sábado del rango, o 0 si el rango no es válido
     */
    private static int validarRangoDeFechas(LocalDate inicio, LocalDate fin,
                                            List<String> errores, LocalDate hoy) {
        if (inicio == null) {
            errores.add("fechaInicio: es obligatoria");
        }
        if (fin == null) {
            errores.add("fechaFin: es obligatoria");
        }
        if (inicio == null || fin == null) {
            return 0;
        }

        if (fin.isBefore(inicio)) {
            errores.add("fechaFin: no puede ser anterior a fechaInicio");
            return 0;
        }
        if (hoy != null && inicio.isBefore(hoy)) {
            errores.add("fechaInicio: no puede ser una fecha pasada");
            return 0;
        }

        int dias = ReglasVacaciones.contarDiasLaborables(inicio, fin);
        if (dias == 0) {
            errores.add(ERROR_SOLO_DOMINGOS);
        }
        return dias;
    }

    private static void validarMotivo(String motivo, List<String> errores) {
        String limpio = (motivo == null) ? "" : motivo.trim();

        if (limpio.isEmpty()) {
            errores.add("motivo: es obligatorio");
        } else if (limpio.length() < MOTIVO_MIN || limpio.length() > MOTIVO_MAX) {
            errores.add("motivo: debe tener entre " + MOTIVO_MIN + " y " + MOTIVO_MAX + " caracteres");
        }
    }

    private static void lanzarSiHayErrores(List<String> errores) throws ValidacionException {
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }
}
