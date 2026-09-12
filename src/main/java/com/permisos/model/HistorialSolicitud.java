package com.permisos.model;

import java.time.LocalDateTime;

/**
 * Registro historico de cada cambio de estado de una Solicitud
 * (quien lo hizo, cuando y con que comentario).
 */
public class HistorialSolicitud {

    private int idHistorial;
    private int idSolicitud;
    private String estadoAnterior;
    private String estadoNuevo;
    private String comentario;
    private int idUsuario;
    private LocalDateTime fecha;

    public HistorialSolicitud() {
    }

    public int getIdHistorial() { return idHistorial; }
    public void setIdHistorial(int idHistorial) { this.idHistorial = idHistorial; }

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}