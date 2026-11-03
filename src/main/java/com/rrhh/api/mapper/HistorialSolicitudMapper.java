package com.rrhh.api.mapper;

import com.permisos.model.HistorialSolicitud;
import com.rrhh.api.dto.HistorialSolicitudDTO;

public class HistorialSolicitudMapper {

    private HistorialSolicitudMapper() {
    }

    public static HistorialSolicitudDTO toDTO(HistorialSolicitud h) {
        if (h == null) {
            return null;
        }

        HistorialSolicitudDTO dto = new HistorialSolicitudDTO();

        dto.setId(h.getIdHistorial());
        dto.setSolicitudId(h.getIdSolicitud());
        dto.setEstadoAnterior(h.getEstadoAnterior());
        dto.setEstadoNuevo(h.getEstadoNuevo());
        dto.setComentario(h.getComentario());
        // el usuario puede ser null en el historial
        dto.setUsuarioId(h.getUsuario() != null ? h.getUsuario().getIdUsuario() : null);
        dto.setFecha(h.getFecha());

        return dto;
    }
}