package com.rrhh.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// cuerpo de aprobar/rechazar constancia; en aprobar todo es opcional
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResolverConstanciaDTO {

    private String motivoRechazo;
    private String observaciones;

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}