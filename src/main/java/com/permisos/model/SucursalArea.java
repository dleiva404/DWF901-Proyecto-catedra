package com.permisos.model;

/**
 * Catalogo de sucursales / areas de la empresa.
 */
public class SucursalArea {

    private int idSucursalArea;
    private String nombre;
    private String descripcion;
    private boolean activo;

    public SucursalArea() {
    }

    public SucursalArea(int idSucursalArea, String nombre, String descripcion, boolean activo) {
        this.idSucursalArea = idSucursalArea;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public int getIdSucursalArea() { return idSucursalArea; }
    public void setIdSucursalArea(int idSucursalArea) { this.idSucursalArea = idSucursalArea; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}