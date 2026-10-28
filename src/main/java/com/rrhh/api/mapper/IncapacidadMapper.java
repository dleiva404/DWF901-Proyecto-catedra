
package com.rrhh.api.mapper;

import com.permisos.model.Incapacidad;
import com.rrhh.api.dto.IncapacidadDTO;

public class IncapacidadMapper {

    private IncapacidadMapper() {
    }

    public static IncapacidadDTO toDTO(Incapacidad incapacidad) {
        if (incapacidad == null) {
            return null;
        }

        IncapacidadDTO dto = new IncapacidadDTO();

        dto.setId(incapacidad.getIdIncapacidad());
        dto.setSolicitudId(incapacidad.getIdSolicitud());
        dto.setNumeroDocumento(incapacidad.getNumeroDocumento());
        dto.setFechaInicio(incapacidad.getFechaInicio());
        dto.setFechaFin(incapacidad.getFechaFin());
        dto.setDocumentoNombre(incapacidad.getDocumentoNombre());
        dto.setObservaciones(incapacidad.getObservaciones());

        return dto;
    }
}
