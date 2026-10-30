package com.permisos.service;

/**
 * Saldo de vacaciones de un empleado en un año, listo para devolverse en
 * GET /api/empleados/me/vacaciones.
 *
 * Significado de cada campo (conviene que el contrato de la API los repita):
 *  - diasAsignados:   días que le corresponden ese año.
 *  - diasTomados:     días ya consumidos (solicitudes APROBADAS).
 *  - diasPendientes:  días comprometidos en solicitudes PENDIENTES de respuesta.
 *  - diasDisponibles: asignados - tomados. Es el mismo número que muestra la
 *                     pantalla web en el anillo de saldo (no descuenta los pendientes).
 *  - diasSolicitables: lo que todavía se puede pedir ahora mismo =
 *                     disponibles - pendientes (nunca menor que 0). Es el límite
 *                     real que aplica la validación al crear una solicitud.
 *
 * Es Java puro (sin JPA) para poder probarlo sin base de datos.
 */
public final class ResumenVacaciones {

    private final int anio;
    private final int diasAsignados;
    private final int diasTomados;
    private final int diasPendientes;
    private final int diasDisponibles;
    private final int diasSolicitables;

    private ResumenVacaciones(int anio, int diasAsignados, int diasTomados,
                              int diasPendientes, int diasDisponibles, int diasSolicitables) {
        this.anio = anio;
        this.diasAsignados = diasAsignados;
        this.diasTomados = diasTomados;
        this.diasPendientes = diasPendientes;
        this.diasDisponibles = diasDisponibles;
        this.diasSolicitables = diasSolicitables;
    }

    /**
     * Arma el resumen a partir de lo que guarda la base de datos.
     *
     * @param disponibles lo que dice la columna dias_disponibles (no descuenta pendientes)
     * @param pendientes  suma de días de las solicitudes PENDIENTES de vacaciones del año
     * @throws IllegalArgumentException si algún valor es negativo (sería un dato corrupto)
     */
    public static ResumenVacaciones calcular(int anio, int asignados, int tomados,
                                             int disponibles, int pendientes) {
        if (asignados < 0 || tomados < 0 || disponibles < 0 || pendientes < 0) {
            throw new IllegalArgumentException("Los días de vacaciones no pueden ser negativos.");
        }
        int solicitables = Math.max(0, disponibles - pendientes);
        return new ResumenVacaciones(anio, asignados, tomados, pendientes, disponibles, solicitables);
    }

    public int getAnio() {
        return anio;
    }

    public int getDiasAsignados() {
        return diasAsignados;
    }

    public int getDiasTomados() {
        return diasTomados;
    }

    public int getDiasPendientes() {
        return diasPendientes;
    }

    public int getDiasDisponibles() {
        return diasDisponibles;
    }

    public int getDiasSolicitables() {
        return diasSolicitables;
    }
}
