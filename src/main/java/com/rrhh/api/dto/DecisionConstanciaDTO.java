
package com.rrhh.api.dto;

public class DecisionConstanciaDTO {

    private String motivoRechazo;
    private String observaciones;

    public DecisionConstanciaDTO() {
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
