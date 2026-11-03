package com.rrhh.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// cuerpo de POST /constancias; todo como texto para validarlo nosotros
@JsonIgnoreProperties(ignoreUnknown = true)
public class NuevaConstanciaDTO {

    private String tipo;
    private String institucion;
    private String motivo;

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) { this.institucion = institucion; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}