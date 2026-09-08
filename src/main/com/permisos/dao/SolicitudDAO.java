package com.permisos.dao;

import com.permisos.model.HistorialSolicitud;
import com.permisos.model.Solicitud;
import com.permisos.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SolicitudDAO {

    private final HistorialSolicitudDAO historialDAO = new HistorialSolicitudDAO();

    private static final String SQL_INSERTAR =
            "INSERT INTO solicitudes (id_empleado, id_tipo_solicitud, fecha_inicio, fecha_fin, " +
            "dias_solicitados, motivo, estado) VALUES (?, ?, ?, ?, ?, ?, 'PENDIENTE')";

    private static final String SQL_LISTAR_POR_EMPLEADO =
            "SELECT id_solicitud, id_empleado, id_tipo_solicitud, fecha_solicitud, fecha_inicio, fecha_fin, " +
            "dias_solicitados, motivo, estado, motivo_rechazo, fecha_respuesta, id_jefatura_respuesta, " +
            "observaciones_rrhh, fecha_recepcion_rrhh " +
            "FROM solicitudes WHERE id_empleado = ? ORDER BY fecha_solicitud DESC";

    private static final String SQL_LISTAR_PENDIENTES_POR_JEFATURA =
            "SELECT s.id_solicitud, s.id_empleado, s.id_tipo_solicitud, s.fecha_solicitud, s.fecha_inicio, " +
            "s.fecha_fin, s.dias_solicitados, s.motivo, s.estado, s.motivo_rechazo, s.fecha_respuesta, " +
            "s.id_jefatura_respuesta, s.observaciones_rrhh, s.fecha_recepcion_rrhh " +
            "FROM solicitudes s " +
            "JOIN empleados e ON s.id_empleado = e.id_empleado " +
            "JOIN jefaturas j ON j.id_departamento = e.id_departamento " +
            "WHERE j.id_empleado = ? AND j.activo = TRUE AND s.estado = 'PENDIENTE' " +
            "ORDER BY s.fecha_solicitud ASC";

    private static final String SQL_LISTAR_PARA_RRHH =
            "SELECT id_solicitud, id_empleado, id_tipo_solicitud, fecha_solicitud, fecha_inicio, fecha_fin, " +
            "dias_solicitados, motivo, estado, motivo_rechazo, fecha_respuesta, id_jefatura_respuesta, " +
            "observaciones_rrhh, fecha_recepcion_rrhh " +
            "FROM solicitudes WHERE estado IN ('APROBADA','RECHAZADA') ORDER BY fecha_respuesta DESC";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id_solicitud, id_empleado, id_tipo_solicitud, fecha_solicitud, fecha_inicio, fecha_fin, " +
            "dias_solicitados, motivo, estado, motivo_rechazo, fecha_respuesta, id_jefatura_respuesta, " +
            "observaciones_rrhh, fecha_recepcion_rrhh FROM solicitudes WHERE id_solicitud = ?";

    private static final String SQL_APROBAR =
            "UPDATE solicitudes SET estado = 'APROBADA', fecha_respuesta = NOW(), id_jefatura_respuesta = ? " +
            "WHERE id_solicitud = ?";

    private static final String SQL_RECHAZAR =
            "UPDATE solicitudes SET estado = 'RECHAZADA', fecha_respuesta = NOW(), id_jefatura_respuesta = ?, " +
            "motivo_rechazo = ? WHERE id_solicitud = ?";

    private static final String SQL_CANCELAR =
            "UPDATE solicitudes SET estado = 'CANCELADA' WHERE id_solicitud = ? AND estado = 'PENDIENTE'";

    private static final String SQL_MARCAR_RECIBIDA_RRHH =
            "UPDATE solicitudes SET observaciones_rrhh = ?, fecha_recepcion_rrhh = NOW() WHERE id_solicitud = ?";

    public int insertar(Solicitud s) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getIdEmpleado());
            ps.setInt(2, s.getIdTipoSolicitud());
            ps.setDate(3, Date.valueOf(s.getFechaInicio()));
            ps.setDate(4, Date.valueOf(s.getFechaFin()));
            ps.setInt(5, s.getDiasSolicitados());
            ps.setString(6, s.getMotivo());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo registrar la solicitud.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id de la solicitud generada.");
    }

    public List<Solicitud> listarPorEmpleado(int idEmpleado) throws SQLException {
        List<Solicitud> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_POR_EMPLEADO)) {

            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<Solicitud> listarPendientesPorJefatura(int idEmpleadoJefe) throws SQLException {
        List<Solicitud> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_PENDIENTES_POR_JEFATURA)) {

            ps.setInt(1, idEmpleadoJefe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<Solicitud> listarParaRRHH() throws SQLException {
        List<Solicitud> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_PARA_RRHH);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Solicitud buscarPorId(int idSolicitud) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /**
     * Aprueba una solicitud y registra el historial en la misma transacción,
     * para que ambos cambios se confirmen (o fallen) juntos.
     */
    public void aprobar(int idSolicitud, int idEmpleadoJefe, int idUsuarioJefatura) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_APROBAR)) {
                ps.setInt(1, idEmpleadoJefe);
                ps.setInt(2, idSolicitud);
                ps.executeUpdate();
            }

            HistorialSolicitud h = new HistorialSolicitud();
            h.setIdSolicitud(idSolicitud);
            h.setEstadoAnterior("PENDIENTE");
            h.setEstadoNuevo("APROBADA");
            h.setIdUsuario(idUsuarioJefatura);
            historialDAO.insertar(con, h);

            con.commit();
        }
    }

    /**
     * Rechaza una solicitud (exige motivo) y registra el historial en la
     * misma transacción.
     */
    public void rechazar(int idSolicitud, int idEmpleadoJefe, String motivoRechazo, int idUsuarioJefatura)
            throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_RECHAZAR)) {
                ps.setInt(1, idEmpleadoJefe);
                ps.setString(2, motivoRechazo);
                ps.setInt(3, idSolicitud);
                ps.executeUpdate();
            }

            HistorialSolicitud h = new HistorialSolicitud();
            h.setIdSolicitud(idSolicitud);
            h.setEstadoAnterior("PENDIENTE");
            h.setEstadoNuevo("RECHAZADA");
            h.setComentario(motivoRechazo);
            h.setIdUsuario(idUsuarioJefatura);
            historialDAO.insertar(con, h);

            con.commit();
        }
    }

    /**
     * Cancela una solicitud propia, solo si sigue PENDIENTE
     * (no se puede cancelar algo que ya fue aprobado o rechazado).
     */
    public boolean cancelar(int idSolicitud, int idUsuario) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            int filas;
            try (PreparedStatement ps = con.prepareStatement(SQL_CANCELAR)) {
                ps.setInt(1, idSolicitud);
                filas = ps.executeUpdate();
            }

            if (filas > 0) {
                HistorialSolicitud h = new HistorialSolicitud();
                h.setIdSolicitud(idSolicitud);
                h.setEstadoAnterior("PENDIENTE");
                h.setEstadoNuevo("CANCELADA");
                h.setIdUsuario(idUsuario);
                historialDAO.insertar(con, h);
            }

            con.commit();
            return filas > 0;
        }
    }

    /** RRHH marca una solicitud ya resuelta como "recibida/procesada" con sus observaciones. */
    public void marcarRecibidaPorRRHH(int idSolicitud, String observaciones) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_MARCAR_RECIBIDA_RRHH)) {
            ps.setString(1, observaciones);
            ps.setInt(2, idSolicitud);
            ps.executeUpdate();
        }
    }

    private Solicitud mapear(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();
        s.setIdSolicitud(rs.getInt("id_solicitud"));
        s.setIdEmpleado(rs.getInt("id_empleado"));
        s.setIdTipoSolicitud(rs.getInt("id_tipo_solicitud"));

        Timestamp fechaSolicitud = rs.getTimestamp("fecha_solicitud");
        if (fechaSolicitud != null) {
            s.setFechaSolicitud(fechaSolicitud.toLocalDateTime());
        }

        Date fechaInicio = rs.getDate("fecha_inicio");
        if (fechaInicio != null) {
            s.setFechaInicio(fechaInicio.toLocalDate());
        }
        Date fechaFin = rs.getDate("fecha_fin");
        if (fechaFin != null) {
            s.setFechaFin(fechaFin.toLocalDate());
        }

        s.setDiasSolicitados(rs.getInt("dias_solicitados"));
        s.setMotivo(rs.getString("motivo"));
        s.setEstado(rs.getString("estado"));
        s.setMotivoRechazo(rs.getString("motivo_rechazo"));

        Timestamp fechaRespuesta = rs.getTimestamp("fecha_respuesta");
        if (fechaRespuesta != null) {
            s.setFechaRespuesta(fechaRespuesta.toLocalDateTime());
        }

        int idJefaturaRespuesta = rs.getInt("id_jefatura_respuesta");
        s.setIdJefaturaRespuesta(rs.wasNull() ? null : idJefaturaRespuesta);

        s.setObservacionesRrhh(rs.getString("observaciones_rrhh"));

        Timestamp fechaRecepcionRrhh = rs.getTimestamp("fecha_recepcion_rrhh");
        if (fechaRecepcionRrhh != null) {
            s.setFechaRecepcionRrhh(fechaRecepcionRrhh.toLocalDateTime());
        }

        return s;
    }
}
