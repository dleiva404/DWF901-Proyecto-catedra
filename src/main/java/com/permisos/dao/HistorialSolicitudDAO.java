package com.permisos.dao;

import com.permisos.model.HistorialSolicitud;
import com.permisos.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HistorialSolicitudDAO {

    private static final String SQL_INSERTAR =
            "INSERT INTO historial_solicitud (id_solicitud, estado_anterior, estado_nuevo, comentario, id_usuario) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_LISTAR_POR_SOLICITUD =
            "SELECT id_historial, id_solicitud, estado_anterior, estado_nuevo, comentario, id_usuario, fecha " +
            "FROM historial_solicitud WHERE id_solicitud = ? ORDER BY fecha ASC";

    /**
     * Inserta usando una conexión ya abierta por el llamador (por ejemplo,
     * dentro de la misma transacción de SolicitudDAO.aprobar()/rechazar()),
     * para que el cambio de estado y el historial se confirmen juntos.
     */
    public void insertar(Connection con, HistorialSolicitud h) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR)) {
            ps.setInt(1, h.getIdSolicitud());
            ps.setString(2, h.getEstadoAnterior());
            ps.setString(3, h.getEstadoNuevo());
            ps.setString(4, h.getComentario());
            ps.setInt(5, h.getIdUsuario());
            ps.executeUpdate();
        }
    }

    /** Versión independiente, que abre y cierra su propia conexión. */
    public void insertar(HistorialSolicitud h) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            insertar(con, h);
        }
    }

    public List<HistorialSolicitud> listarPorSolicitud(int idSolicitud) throws SQLException {
        List<HistorialSolicitud> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_POR_SOLICITUD)) {

            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialSolicitud h = new HistorialSolicitud();
                    h.setIdHistorial(rs.getInt("id_historial"));
                    h.setIdSolicitud(rs.getInt("id_solicitud"));
                    h.setEstadoAnterior(rs.getString("estado_anterior"));
                    h.setEstadoNuevo(rs.getString("estado_nuevo"));
                    h.setComentario(rs.getString("comentario"));
                    h.setIdUsuario(rs.getInt("id_usuario"));

                    Timestamp fecha = rs.getTimestamp("fecha");
                    if (fecha != null) {
                        h.setFecha(fecha.toLocalDateTime());
                    }
                    lista.add(h);
                }
            }
        }
        return lista;
    }
}
