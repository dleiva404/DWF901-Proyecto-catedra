package com.permisos.model;

/**
 * Indica que un Empleado es jefe de un Departamento y/o de una
 * Sucursal/Area. idDepartamento e idSucursalArea son Integer (no int)
 * a proposito, porque ambos pueden quedar en null.
 */
public class Jefatura {

    private int idJefatura;
    private int idEmpleado;
    private Integer idDepartamento;
    private Integer idSucursalArea;
    private boolean activo;

    public Jefatura() {
    }

    public int getIdJefatura() { return idJefatura; }
    public void setIdJefatura(int idJefatura) { this.idJefatura = idJefatura; }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public Integer getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Integer idDepartamento) { this.idDepartamento = idDepartamento; }

    public Integer getIdSucursalArea() { return idSucursalArea; }
    public void setIdSucursalArea(Integer idSucursalArea) { this.idSucursalArea = idSucursalArea; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}