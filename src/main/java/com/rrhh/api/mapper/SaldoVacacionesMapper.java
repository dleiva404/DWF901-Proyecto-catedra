package com.rrhh.api.mapper;

import com.permisos.service.ResumenVacaciones;
import com.rrhh.api.dto.SaldoVacacionesDTO;

public final class SaldoVacacionesMapper {

    private SaldoVacacionesMapper() {
    }

    /**
     * El contrato llama diasUtilizados a lo que el servicio llama diasTomados
     * (días ya consumidos por solicitudes aprobadas).
     */
    public static SaldoVacacionesDTO toDTO(int empleadoId, ResumenVacaciones resumen) {
        if (resumen == null) {
            return null;
        }

        SaldoVacacionesDTO dto = new SaldoVacacionesDTO();
        dto.setEmpleadoId(empleadoId);
        dto.setAnio(resumen.getAnio());
        dto.setDiasAsignados(resumen.getDiasAsignados());
        dto.setDiasUtilizados(resumen.getDiasTomados());
        dto.setDiasDisponibles(resumen.getDiasDisponibles());
        dto.setDiasPendientes(resumen.getDiasPendientes());
        return dto;
    }
}
