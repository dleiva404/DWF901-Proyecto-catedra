package com.rrhh.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class HistorialSolicitudDTO {

    private Integer id;
    private Integer solicitudId;
    private String estadoAnterior;
    private String estadoNuevo;
    private String comentario;
    private Integer usuarioId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSolicitudId() { return solicitudId; }
    public void setSolicitudId(Integer solicitudId) { this.solicitudId = solicitudId; }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}