package com.permisos.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
public class Solicitud implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_APROBADA = "APROBADA";
    public static final String ESTADO_RECHAZADA = "RECHAZADA";
    public static final String ESTADO_CANCELADA = "CANCELADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private int idSolicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_solicitud", nullable = false)
    private TipoSolicitud tipoSolicitud;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "dias_solicitados", nullable = false)
    private int diasSolicitados;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    /*
     * Esta FK apunta a empleados.id_empleado,
     * no a jefaturas.id_jefatura.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jefatura_respuesta")
    private Empleado jefaturaRespuesta;

    @Column(name = "observaciones_rrhh", columnDefinition = "TEXT")
    private String observacionesRrhh;

    @Column(name = "fecha_recepcion_rrhh")
    private LocalDateTime fechaRecepcionRrhh;

    public Solicitud() {
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public int getIdEmpleado() {
        return empleado != null ? empleado.getIdEmpleado() : 0;
    }

    public void setIdEmpleado(int idEmpleado) {
        if (this.empleado == null) {
            this.empleado = new Empleado();
        }
        this.empleado.setIdEmpleado(idEmpleado);
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public int getIdTipoSolicitud() {
        return tipoSolicitud != null ? tipoSolicitud.getIdTipoSolicitud() : 0;
    }

    public void setIdTipoSolicitud(int idTipoSolicitud) {
        if (this.tipoSolicitud == null) {
            this.tipoSolicitud = new TipoSolicitud();
        }
        this.tipoSolicitud.setIdTipoSolicitud(idTipoSolicitud);
    }

    public TipoSolicitud getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(TipoSolicitud tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getDiasSolicitados() {
        return diasSolicitados;
    }

    public void setDiasSolicitados(int diasSolicitados) {
        this.diasSolicitados = diasSolicitados;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public Integer getIdJefaturaRespuesta() {
        return jefaturaRespuesta != null
                ? jefaturaRespuesta.getIdEmpleado()
                : null;
    }

    public void setIdJefaturaRespuesta(Integer idJefaturaRespuesta) {
        if (idJefaturaRespuesta == null) {
            this.jefaturaRespuesta = null;
        } else {
            if (this.jefaturaRespuesta == null) {
                this.jefaturaRespuesta = new Empleado();
            }
            this.jefaturaRespuesta.setIdEmpleado(idJefaturaRespuesta);
        }
    }

    public Empleado getJefaturaRespuesta() {
        return jefaturaRespuesta;
    }

    public void setJefaturaRespuesta(Empleado jefaturaRespuesta) {
        this.jefaturaRespuesta = jefaturaRespuesta;
    }

    public String getObservacionesRrhh() {
        return observacionesRrhh;
    }

    public void setObservacionesRrhh(String observacionesRrhh) {
        this.observacionesRrhh = observacionesRrhh;
    }

    public LocalDateTime getFechaRecepcionRrhh() {
        return fechaRecepcionRrhh;
    }

    public void setFechaRecepcionRrhh(LocalDateTime fechaRecepcionRrhh) {
        this.fechaRecepcionRrhh = fechaRecepcionRrhh;
    }
}