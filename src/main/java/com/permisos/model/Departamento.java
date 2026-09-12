package com.permisos.model;

//Clase para heredar los datos básicos
public class Departamento {
    protected String codigo;
    protected String nombres;
    protected String apellidos;

    // Constructor principal
    public Departamento(String codigo, String nombres, String apellidos) {
        this.codigo = codigo;
        this.nombres = nombres;
        this.apellidos = apellidos;
    }

    public Departamento(int idDepartamento, String nombre, String descripcion, boolean activo) {
    }

    // Métodos Getters y Setters
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
}