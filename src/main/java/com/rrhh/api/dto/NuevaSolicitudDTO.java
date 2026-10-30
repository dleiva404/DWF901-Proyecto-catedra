package com.rrhh.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Cuerpo de POST /solicitudes. Las fechas llegan como texto (yyyy-MM-dd) a
 * propósito: así un formato inválido se reporta como un 400 con la lista
 * "campos", en lugar de un error genérico de Jackson. El cliente nunca envía
 * diasSolicitados: lo calcula el servidor.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NuevaSolicitudDTO {

    private Integer tipoSolicitudId;
    private String fechaInicio;
    private String fechaFin;
    private String motivo;

    public Integer getTipoSolicitudId() { return tipoSolicitudId; }
    public void setTipoSolicitudId(Integer tipoSolicitudId) { this.tipoSolicitudId = tipoSolicitudId; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
