package com.rrhh.api.dto;

/**
 * Respuesta de GET /vacaciones/saldo.
 *
 * diasPendientes es un campo que se agrega a los que ya define el contrato v1.2
 * (empleadoId, anio, diasAsignados, diasUtilizados, diasDisponibles), porque la
 * especificación de la parte 3 pide mostrar el saldo con días tomados,
 * pendientes y disponibles. Agregar un campo no rompe a los clientes
 * existentes, pero el contrato debe actualizarse.
 */
public class SaldoVacacionesDTO {

    private Integer empleadoId;
    private Integer anio;
    private Integer diasAsignados;
    private Integer diasUtilizados;
    private Integer diasDisponibles;
    private Integer diasPendientes;

    public Integer getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(Integer empleadoId) { this.empleadoId = empleadoId; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public Integer getDiasAsignados() { return diasAsignados; }
    public void setDiasAsignados(Integer diasAsignados) { this.diasAsignados = diasAsignados; }

    public Integer getDiasUtilizados() { return diasUtilizados; }
    public void setDiasUtilizados(Integer diasUtilizados) { this.diasUtilizados = diasUtilizados; }

    public Integer getDiasDisponibles() { return diasDisponibles; }
    public void setDiasDisponibles(Integer diasDisponibles) { this.diasDisponibles = diasDisponibles; }

    public Integer getDiasPendientes() { return diasPendientes; }
    public void setDiasPendientes(Integer diasPendientes) { this.diasPendientes = diasPendientes; }
}
