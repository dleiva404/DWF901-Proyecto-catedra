package com.rrhh.api.mapper;

import com.permisos.model.Rol;
import com.permisos.model.Usuario;
import com.rrhh.api.dto.RolDTO;
import com.rrhh.api.dto.UsuarioAdminDTO;

/** Convierte usuarios y roles a DTO. Nunca copia la contrasena. */
public final class UsuarioAdminMapper {

    private UsuarioAdminMapper() {
    }

    public static UsuarioAdminDTO toDTO(Usuario usuario) {
        UsuarioAdminDTO dto = new UsuarioAdminDTO();
        dto.setId(usuario.getIdUsuario());
        dto.setUsername(usuario.getUsername());
        dto.setActivo(usuario.isActivo());
        dto.setUltimoAcceso(usuario.getUltimoAcceso());

        if (usuario.getEmpleado() != null) {
            dto.setEmpleadoId(usuario.getEmpleado().getIdEmpleado());
            dto.setEmpleadoNombre(usuario.getEmpleado().getNombre() + " "
                    + usuario.getEmpleado().getApellido());
        }
        if (usuario.getRol() != null) {
            dto.setRolId(usuario.getRol().getIdRol());
            dto.setRol(usuario.getRol().getNombre());
        }
        return dto;
    }

    public static RolDTO toDTO(Rol rol) {
        RolDTO dto = new RolDTO();
        dto.setId(rol.getIdRol());
        dto.setNombre(rol.getNombre());
        dto.setDescripcion(rol.getDescripcion());
        return dto;
    }
}
