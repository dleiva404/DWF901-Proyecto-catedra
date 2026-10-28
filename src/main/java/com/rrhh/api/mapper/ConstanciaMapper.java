
package com.rrhh.api.mapper;

import com.permisos.model.Constancia;
import com.rrhh.api.dto.ConstanciaDTO;

public class ConstanciaMapper {

    private ConstanciaMapper() {
    }

    public static ConstanciaDTO toDTO(Constancia constancia) {
        if (constancia == null) {
            return null;
        }

        ConstanciaDTO dto = new ConstanciaDTO();

        dto.setId(constancia.getIdConstancia());
        dto.setEmpleadoId(constancia.getIdEmpleado());
        dto.setTipo(constancia.getTipo());
        dto.setInstitucion(constancia.getInstitucion());
        dto.setMotivo(constancia.getMotivo());
        dto.setEmpresaEmisora(constancia.getEmpresaEmisora());
        dto.setFechaSolicitud(constancia.getFechaSolicitud());
        dto.setEstado(constancia.getEstado());
        dto.setFechaRespuesta(constancia.getFechaRespuesta());

        return dto;
    }
}
