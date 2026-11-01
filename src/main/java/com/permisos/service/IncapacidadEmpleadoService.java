package com.permisos.service;

import com.permisos.dao.IncapacidadDAO;
import com.permisos.dao.TipoSolicitudDAO;
import com.permisos.model.Incapacidad;
import com.permisos.model.Solicitud;
import com.permisos.model.TipoSolicitud;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Registro de una incapacidad por parte del empleado.
 *
 * Una incapacidad son DOS registros que deben existir juntos o no existir: la
 * solicitud (lo que revisa la jefatura) y el detalle de la incapacidad
 * (documento ISSS, fechas, observaciones). Se guardan en UNA sola transacción
 * con IncapacidadDAO.registrar, que ya hace exactamente eso; este servicio le
 * suma las validaciones y arma los dos objetos igual que el formulario web.
 *
 * Mapeo a HTTP (lo hace la capa REST): ValidacionException -> 400,
 * SQLException -> 500.
 */
public class IncapacidadEmpleadoService {

    private static final String TIPO_INCAPACIDAD = "INCAPACIDAD";

    private final TipoSolicitudDAO tipoSolicitudDAO = new TipoSolicitudDAO();
    private final IncapacidadDAO incapacidadDAO = new IncapacidadDAO();

    /**
     * Valida y registra la incapacidad. Devuelve la incapacidad ya guardada,
     * con su id y el id de su solicitud ({@code getIdSolicitud()}).
     *
     * El documento adjunto (archivo) no se maneja aquí: la API JSON recibe solo
     * los datos; subir archivos requeriría un endpoint multipart aparte.
     */
    public Incapacidad registrar(int idEmpleado, String numeroDocumento,
                                 LocalDate inicio, LocalDate fin, String observaciones)
            throws ReglaNegocioException, SQLException {

        int dias = ReglasSolicitud.validarIncapacidad(numeroDocumento, observaciones, inicio, fin);

        TipoSolicitud tipo = tipoSolicitudDAO.buscarPorNombre(TIPO_INCAPACIDAD);
        if (tipo == null) {
            // Es un problema del catálogo de datos, no del usuario: se reporta como error interno.
            throw new SQLException("No se encontró el tipo de solicitud INCAPACIDAD en el catálogo.");
        }

        String documento = numeroDocumento.trim();

        Solicitud solicitud = new Solicitud();
        solicitud.setIdEmpleado(idEmpleado);
        solicitud.setIdTipoSolicitud(tipo.getIdTipoSolicitud());
        solicitud.setFechaInicio(inicio);
        solicitud.setFechaFin(fin);
        solicitud.setDiasSolicitados(dias);
        // Mismo texto que genera el formulario web, para que ambos canales sean iguales.
        solicitud.setMotivo("Incapacidad medica - documento " + documento);

        Incapacidad incapacidad = new Incapacidad();
        incapacidad.setNumeroDocumento(documento);
        incapacidad.setFechaInicio(inicio);
        incapacidad.setFechaFin(fin);
        incapacidad.setObservaciones(observaciones != null ? observaciones.trim() : null);

        // Solicitud + incapacidad en una sola transacción.
        incapacidadDAO.registrar(solicitud, incapacidad);

        return incapacidad;
    }
}
