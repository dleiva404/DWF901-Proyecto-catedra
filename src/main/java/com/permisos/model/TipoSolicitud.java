package com.permisos.model;

/**
 * Catalogo de tipos de solicitud (VACACIONES, INCAPACIDAD, AUSENCIA).
 */
public class TipoSolicitud {

    private int idTipoSolicitud;
    private String nombre;
    private String descripcion;
    private boolean activo;

    public TipoSolicitud() {
    }

    public TipoSolicitud(int idTipoSolicitud, String nombre, String descripcion, boolean activo) {
        this.idTipoSolicitud = idTipoSolicitud;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public int getIdTipoSolicitud() { return idTipoSolicitud; }
    public void setIdTipoSolicitud(int idTipoSolicitud) { this.idTipoSolicitud = idTipoSolicitud; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}