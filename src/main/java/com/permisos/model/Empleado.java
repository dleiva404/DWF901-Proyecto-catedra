package com.permisos.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "empleados")
public class Empleado implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private int idEmpleado;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "dui", unique = true, length = 10)
    private String dui;

    @Column(name = "correo", unique = true, length = 150)
    private String correo;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sucursal_area", nullable = false)
    private SucursalArea sucursalArea;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departamento", nullable = false)
    private Departamento departamento;

    @Column(name = "cargo", length = 100)
    private String cargo;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public Empleado() {
    }

    public Empleado(String nombre, String apellido, String dui, String correo,
                    String telefono, int idSucursalArea, int idDepartamento,
                    String cargo, LocalDate fechaIngreso) {

        this.nombre = nombre;
        this.apellido = apellido;
        this.dui = dui;
        this.correo = correo;
        this.telefono = telefono;

        this.cargo = cargo;
        this.fechaIngreso = fechaIngreso;
        this.activo = true;
        this.sucursalArea = new SucursalArea();
        this.sucursalArea.setIdSucursalArea(idSucursalArea);

        this.departamento = new Departamento();
        this.departamento.setIdDepartamento(idDepartamento);
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDui() {
        return dui;
    }

    public void setDui(String dui) {
        this.dui = dui;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getIdSucursalArea() {
        return sucursalArea != null ? sucursalArea.getIdSucursalArea() : 0;
    }

    public void setIdSucursalArea(int idSucursalArea) {
        if (this.sucursalArea == null) {
            this.sucursalArea = new SucursalArea();
        }
        this.sucursalArea.setIdSucursalArea(idSucursalArea);
    }

    public SucursalArea getSucursalArea() {
        return sucursalArea;
    }

    public void setSucursalArea(SucursalArea sucursalArea) {
        this.sucursalArea = sucursalArea;
    }

    public int getIdDepartamento() {
        return departamento != null ? departamento.getIdDepartamento() : 0;
    }

    public void setIdDepartamento(int idDepartamento) {
        if (this.departamento == null) {
            this.departamento = new Departamento();
        }
        this.departamento.setIdDepartamento(idDepartamento);
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}