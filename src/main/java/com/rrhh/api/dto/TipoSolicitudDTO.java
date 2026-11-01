package com.rrhh.api.dto;

/** Un tipo del catálogo de GET /tipos-solicitud. */
public class TipoSolicitudDTO {

    private Integer id;
    private String nombre;
    private String descripcion;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
