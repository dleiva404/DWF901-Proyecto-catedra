package com.rrhh.api.dto;

/** Cuerpo de POST /admin/usuarios. */
public class NuevoUsuarioDTO {

    private Integer empleadoId;
    private String username;
    private Integer rolId;
    private String contrasena;

    public Integer getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(Integer empleadoId) { this.empleadoId = empleadoId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getRolId() { return rolId; }
    public void setRolId(Integer rolId) { this.rolId = rolId; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
