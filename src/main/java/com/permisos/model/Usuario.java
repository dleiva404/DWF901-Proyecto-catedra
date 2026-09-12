package com.permisos.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Usuario de acceso a la plataforma (login), ligado a un Empleado y a un Rol.
 * Se guarda en sesion, por eso implementa Serializable.
 */
public class Usuario implements Serializable {

    private int idUsuario;
    private String username;
    private String password;
    private int idEmpleado;
    private int idRol;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;

    public Usuario() {
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
}