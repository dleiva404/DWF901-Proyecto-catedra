
package com.rrhh.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class EmpleadoDTO {

    private Integer id;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private Integer sucursalAreaId;
    private Integer departamentoId;
    private String cargo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaIngreso;

    private boolean activo;
    private String empresa;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Integer getSucursalAreaId() { return sucursalAreaId; }
    public void setSucursalAreaId(Integer id) { this.sucursalAreaId = id; }

    public Integer getDepartamentoId() { return departamentoId; }
    public void setDepartamentoId(Integer id) { this.departamentoId = id; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
}

