package com.permisos.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Solicitud de un empleado (vacaciones, incapacidad o ausencia) y su ciclo
 * de vida: PENDIENTE -> APROBADA / RECHAZADA / CANCELADA.
 */
public class Solicitud {

    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_APROBADA = "APROBADA";
    public static final String ESTADO_RECHAZADA = "RECHAZADA";
    public static final String ESTADO_CANCELADA = "CANCELADA";

    private int idSolicitud;
    private int idEmpleado;
    private int idTipoSolicitud;
    private LocalDateTime fechaSolicitud;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int diasSolicitados;
    private String motivo;
    private String estado;
    private String motivoRechazo;
    private LocalDateTime fechaRespuesta;
    private Integer idJefaturaRespuesta;
    private String observacionesRrhh;
    private LocalDateTime fechaRecepcionRrhh;

    public Solicitud() {
    }

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public int getIdTipoSolicitud() { return idTipoSolicitud; }
    public void setIdTipoSolicitud(int idTipoSolicitud) { this.idTipoSolicitud = idTipoSolicitud; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public int getDiasSolicitados() { return diasSolicitados; }
    public void setDiasSolicitados(int diasSolicitados) { this.diasSolicitados = diasSolicitados; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }

    public Integer getIdJefaturaRespuesta() { return idJefaturaRespuesta; }
    public void setIdJefaturaRespuesta(Integer idJefaturaRespuesta) { this.idJefaturaRespuesta = idJefaturaRespuesta; }

    public String getObservacionesRrhh() { return observacionesRrhh; }
    public void setObservacionesRrhh(String observacionesRrhh) { this.observacionesRrhh = observacionesRrhh; }

    public LocalDateTime getFechaRecepcionRrhh() { return fechaRecepcionRrhh; }
    public void setFechaRecepcionRrhh(LocalDateTime fechaRecepcionRrhh) { this.fechaRecepcionRrhh = fechaRecepcionRrhh; }
}