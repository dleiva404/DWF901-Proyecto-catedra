package com.rrhh.modelo;
import java.util.Date;

// Hereda de persona para no repetirse codigo, nombres y apellidos
public class Empleado extends Persona {
    private String departamento;
    private String cargo;
    private String correo;
    private String empresa;
    private Date fechaContratacion;
    private Date fechaUltVacacion;
    private double diasAntVacacion;
    private String tipoJornada;

    public Empleado(String codigo, String nombres, String apellidos, String departamento,
                    String cargo, String correo, String empresa, Date fechaContratacion) {
        super(codigo, nombres, apellidos);
        this.departamento = departamento;
        this.cargo = cargo;
        this.correo = correo;
        this.empresa = empresa;
        this.fechaContratacion = fechaContratacion;
        this.diasAntVacacion = 0.0;
    }

    public Date getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(Date fechaContratacion) { this.fechaContratacion = fechaContratacion; }

    public Date getFechaUltVacacion() { return fechaUltVacacion; }
    public void setFechaUltVacacion(Date fechaUltVacacion) { this.fechaUltVacacion = fechaUltVacacion; }

    public double getDiasAntVacacion() { return diasAntVacacion; }
    public void setDiasAntVacacion(double diasAntVacacion) { this.diasAntVacacion = diasAntVacacion; }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}