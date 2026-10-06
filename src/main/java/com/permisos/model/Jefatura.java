package com.permisos.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "jefaturas")
public class Jefatura implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jefatura")
    private int idJefatura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento")
    private Departamento departamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal_area")
    private SucursalArea sucursalArea;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public Jefatura() {
    }

    public int getIdJefatura() {
        return idJefatura;
    }

    public void setIdJefatura(int idJefatura) {
        this.idJefatura = idJefatura;
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

    public Integer getIdDepartamento() {
        return departamento != null ? departamento.getIdDepartamento() : null;
    }

    public void setIdDepartamento(Integer idDepartamento) {
        if (idDepartamento == null) {
            this.departamento = null;
        } else {
            if (this.departamento == null) {
                this.departamento = new Departamento();
            }
            this.departamento.setIdDepartamento(idDepartamento);
        }
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public Integer getIdSucursalArea() {
        return sucursalArea != null ? sucursalArea.getIdSucursalArea() : null;
    }

    public void setIdSucursalArea(Integer idSucursalArea) {
        if (idSucursalArea == null) {
            this.sucursalArea = null;
        } else {
            if (this.sucursalArea == null) {
                this.sucursalArea = new SucursalArea();
            }
            this.sucursalArea.setIdSucursalArea(idSucursalArea);
        }
    }

    public SucursalArea getSucursalArea() {
        return sucursalArea;
    }

    public void setSucursalArea(SucursalArea sucursalArea) {
        this.sucursalArea = sucursalArea;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}