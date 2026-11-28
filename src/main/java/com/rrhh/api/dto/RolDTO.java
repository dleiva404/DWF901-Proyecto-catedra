package com.rrhh.api.dto;

/** Un rol del catalogo de GET /admin/roles. */
public class RolDTO {

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
