
package com.rrhh.api.mapper;

import com.permisos.model.Solicitud;
import com.rrhh.api.dto.SolicitudDTO;

public class SolicitudMapper {

    private SolicitudMapper() {
    }

    public static SolicitudDTO toDTO(Solicitud solicitud) {
        if (solicitud == null) {
            return null;
        }

        SolicitudDTO dto = new SolicitudDTO();

        dto.setId(solicitud.getIdSolicitud());
        dto.setEmpleadoId(solicitud.getIdEmpleado());
        dto.setTipoSolicitudId(solicitud.getIdTipoSolicitud());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud());
        dto.setFechaInicio(solicitud.getFechaInicio());
        dto.setFechaFin(solicitud.getFechaFin());
        dto.setDiasSolicitados(solicitud.getDiasSolicitados());
        dto.setMotivo(solicitud.getMotivo());
        dto.setEstado(solicitud.getEstado());
        dto.setMotivoRechazo(solicitud.getMotivoRechazo());

        return dto;
    }
}
