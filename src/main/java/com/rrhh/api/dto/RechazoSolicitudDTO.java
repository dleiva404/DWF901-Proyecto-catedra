package com.rrhh.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// cuerpo de POST /solicitudes/{id}/rechazar
@JsonIgnoreProperties(ignoreUnknown = true)
public class RechazoSolicitudDTO {

    private String motivoRechazo;

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
}