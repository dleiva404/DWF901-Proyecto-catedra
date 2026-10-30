package com.rrhh.api.mapper;

import com.permisos.model.TipoSolicitud;
import com.rrhh.api.dto.TipoSolicitudDTO;

public final class TipoSolicitudMapper {

    private TipoSolicitudMapper() {
    }

    public static TipoSolicitudDTO toDTO(TipoSolicitud tipo) {
        if (tipo == null) {
            return null;
        }

        TipoSolicitudDTO dto = new TipoSolicitudDTO();
        dto.setId(tipo.getIdTipoSolicitud());
        dto.setNombre(tipo.getNombre());
        dto.setDescripcion(tipo.getDescripcion());
        return dto;
    }
}
