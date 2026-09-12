package com.permisos.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Empleado de la empresa. Se guarda en sesion al iniciar sesion
 * (LoginServlet), por eso implementa Serializable.
 */
public class Empleado implements Serializable {

    private int idEmpleado;
    private String nombre;
    private String apellido;
    private String dui;
    private String correo;
    private String telefono;
    private int idSucursalArea;
    private int idDepartamento;
    private String cargo;
    private LocalDate fechaIngreso;
    private boolean activo;

    public Empleado() {
    }

    public Empleado(String nombre, String apellido, String dui, String correo, String telefono,
                    int idSucursalArea, int idDepartamento, String cargo, LocalDate fechaIngreso) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dui = dui;
        this.correo = correo;
        this.telefono = telefono;
        this.idSucursalArea = idSucursalArea;
        this.idDepartamento = idDepartamento;
        this.cargo = cargo;
        this.fechaIngreso = fechaIngreso;
        this.activo = true;
    }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDui() { return dui; }
    public void setDui(String dui) { this.dui = dui; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public int getIdSucursalArea() { return idSucursalArea; }
    public void setIdSucursalArea(int idSucursalArea) { this.idSucursalArea = idSucursalArea; }

    public int getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(int idDepartamento) { this.idDepartamento = idDepartamento; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}