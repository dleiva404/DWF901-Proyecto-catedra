package com.permisos.model;

import java.time.LocalDate;

/**
 * Detalle de una Solicitud de tipo INCAPACIDAD: numero de documento del
 * ISSS, rango de fechas y el archivo adjunto (nombre + ruta donde se guardo).
 */
public class Incapacidad {

    private int idIncapacidad;
    private int idSolicitud;
    private String numeroDocumento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String documentoNombre;
    private String documentoRuta;
    private String observaciones;

    public Incapacidad() {
    }

    public int getIdIncapacidad() { return idIncapacidad; }
    public void setIdIncapacidad(int idIncapacidad) { this.idIncapacidad = idIncapacidad; }

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getDocumentoNombre() { return documentoNombre; }
    public void setDocumentoNombre(String documentoNombre) { this.documentoNombre = documentoNombre; }

    public String getDocumentoRuta() { return documentoRuta; }
    public void setDocumentoRuta(String documentoRuta) { this.documentoRuta = documentoRuta; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}