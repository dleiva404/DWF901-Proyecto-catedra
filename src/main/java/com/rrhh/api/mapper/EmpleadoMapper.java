
package com.rrhh.api.mapper;

import com.permisos.model.Empleado;
import com.rrhh.api.dto.EmpleadoDTO;

public class EmpleadoMapper {

    private EmpleadoMapper() {
    }

    public static EmpleadoDTO toDTO(Empleado empleado) {
        if (empleado == null) {
            return null;
        }

        EmpleadoDTO dto = new EmpleadoDTO();

        dto.setId(empleado.getIdEmpleado());
        dto.setNombre(empleado.getNombre());
        dto.setApellido(empleado.getApellido());
        dto.setCorreo(empleado.getCorreo());
        dto.setTelefono(empleado.getTelefono());
        dto.setSucursalAreaId(empleado.getIdSucursalArea());
        dto.setDepartamentoId(empleado.getIdDepartamento());
        dto.setCargo(empleado.getCargo());
        dto.setFechaIngreso(empleado.getFechaIngreso());
        dto.setActivo(empleado.isActivo());
        dto.setEmpresa(empleado.getEmpresa());

        return dto;
    }
}
