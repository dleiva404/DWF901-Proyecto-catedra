
package com.rrhh.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ConstanciaDTO {

    private Integer id;
    private Integer empleadoId;
    private String tipo;
    private String institucion;
    private String motivo;
    private String empresaEmisora;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaSolicitud;

    private String estado;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaRespuesta;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(Integer id) { this.empleadoId = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) {
        this.institucion = institucion;
    }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEmpresaEmisora() { return empresaEmisora; }
    public void setEmpresaEmisora(String empresa) {
        this.empresaEmisora = empresa;
    }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fecha) {
        this.fechaSolicitud = fecha;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(LocalDateTime fecha) {
        this.fechaRespuesta = fecha;
    }
}
