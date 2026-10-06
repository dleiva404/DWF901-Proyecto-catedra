package com.permisos.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "incapacidades")
public class Incapacidad implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incapacidad")
    private int idIncapacidad;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false, unique = true)
    private Solicitud solicitud;

    @Column(name = "numero_documento", length = 100)
    private String numeroDocumento;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "documento_nombre", length = 255)
    private String documentoNombre;

    @Column(name = "documento_ruta", length = 500)
    private String documentoRuta;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    public Incapacidad() {
    }

    public int getIdIncapacidad() {
        return idIncapacidad;
    }

    public void setIdIncapacidad(int idIncapacidad) {
        this.idIncapacidad = idIncapacidad;
    }

    public int getIdSolicitud() {
        return solicitud != null ? solicitud.getIdSolicitud() : 0;
    }

    public void setIdSolicitud(int idSolicitud) {
        if (this.solicitud == null) {
            this.solicitud = new Solicitud();
        }
        this.solicitud.setIdSolicitud(idSolicitud);
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
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

    public String getDocumentoNombre() {
        return documentoNombre;
    }

    public void setDocumentoNombre(String documentoNombre) {
        this.documentoNombre = documentoNombre;
    }

    public String getDocumentoRuta() {
        return documentoRuta;
    }

    public void setDocumentoRuta(String documentoRuta) {
        this.documentoRuta = documentoRuta;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}