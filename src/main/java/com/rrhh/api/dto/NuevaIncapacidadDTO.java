package com.rrhh.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Cuerpo de POST /incapacidades. Las fechas llegan como texto (yyyy-MM-dd)
 * por la misma razón que en NuevaSolicitudDTO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NuevaIncapacidadDTO {

    private String numeroDocumento;
    private String fechaInicio;
    private String fechaFin;
    private String observaciones;

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
