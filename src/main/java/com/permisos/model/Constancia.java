package com.permisos.model;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "constancias")
public class Constancia implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_APROBADA = "APROBADA";
    public static final String ESTADO_RECHAZADA = "RECHAZADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_constancia")
    private int idConstancia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "institucion_destino", length = 150)
    private String institucion;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "salario_referencia", precision = 10, scale = 2)
    private BigDecimal salarioReferencia;

    @Column(name = "empresa_emisora", length = 150)
    private String empresaEmisora;

    @Column(name = "token_verificacion", length = 100)
    private String tokenVerificacion;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_rrhh")
    private Usuario usuarioRrhh;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "observaciones_rrhh", columnDefinition = "TEXT")
    private String observacionesRrhh;

    public Constancia() {
    }

    public int getIdConstancia() {
        return idConstancia;
    }

    public void setIdConstancia(int idConstancia) {
        this.idConstancia = idConstancia;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTipoConstancia() {
        return tipo;
    }

    public void setTipoConstancia(String tipoConstancia) {
        this.tipo = tipoConstancia;
    }

    public String getInstitucion() {
        return institucion;
    }

    public void setInstitucion(String institucion) {
        this.institucion = institucion;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public BigDecimal getSalarioReferencia() {
        return salarioReferencia;
    }

    public void setSalarioReferencia(BigDecimal salarioReferencia) {
        this.salarioReferencia = salarioReferencia;
    }

    public String getEmpresaEmisora() {
        return empresaEmisora;
    }

    public void setEmpresaEmisora(String empresaEmisora) {
        this.empresaEmisora = empresaEmisora;
    }

    public String getTokenVerificacion() {
        return tokenVerificacion;
    }

    public void setTokenVerificacion(String tokenVerificacion) {
        this.tokenVerificacion = tokenVerificacion;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public java.sql.Date getFechaCreacion() {
        return fechaSolicitud != null
                ? java.sql.Date.valueOf(fechaSolicitud.toLocalDate())
                : null;
    }

    public void setFechaCreacion(java.sql.Date fechaCreacion) {
        this.fechaSolicitud = fechaCreacion != null
                ? fechaCreacion.toLocalDate().atStartOfDay()
                : null;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public Integer getIdUsuarioRrhh() {
        return usuarioRrhh != null ? usuarioRrhh.getIdUsuario() : null;
    }

    public void setIdUsuarioRrhh(Integer idUsuarioRrhh) {
        if (idUsuarioRrhh == null) {
            this.usuarioRrhh = null;
        } else {
            if (this.usuarioRrhh == null) {
                this.usuarioRrhh = new Usuario();
            }
            this.usuarioRrhh.setIdUsuario(idUsuarioRrhh);
        }
    }

    public Usuario getUsuarioRrhh() {
        return usuarioRrhh;
    }

    public void setUsuarioRrhh(Usuario usuarioRrhh) {
        this.usuarioRrhh = usuarioRrhh;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public String getObservacionesRrhh() {
        return observacionesRrhh;
    }

    public void setObservacionesRrhh(String observacionesRrhh) {
        this.observacionesRrhh = observacionesRrhh;
    }
}
