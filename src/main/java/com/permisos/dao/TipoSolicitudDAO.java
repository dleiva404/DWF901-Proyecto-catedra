package com.permisos.dao;

import com.permisos.model.TipoSolicitud;
import com.permisos.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoSolicitudDAO {

    private static final String SQL_LISTAR_ACTIVOS =
            "SELECT id_tipo_solicitud, nombre, descripcion, activo FROM tipos_solicitud " +
            "WHERE activo = TRUE ORDER BY nombre";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id_tipo_solicitud, nombre, descripcion, activo FROM tipos_solicitud " +
            "WHERE id_tipo_solicitud = ?";

    private static final String SQL_BUSCAR_POR_NOMBRE =
            "SELECT id_tipo_solicitud, nombre, descripcion, activo FROM tipos_solicitud " +
            "WHERE nombre = ?";

    public List<TipoSolicitud> listarActivos() throws SQLException {
        List<TipoSolicitud> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_ACTIVOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public TipoSolicitud buscarPorId(int idTipoSolicitud) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, idTipoSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /**
     * Útil para encontrar rápido el id de 'VACACIONES', 'INCAPACIDAD' o 'AUSENCIA'
     * sin tener que recorrer toda la lista desde el controlador.
     */
    public TipoSolicitud buscarPorNombre(String nombre) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_NOMBRE)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    private TipoSolicitud mapear(ResultSet rs) throws SQLException {
        return new TipoSolicitud(
                rs.getInt("id_tipo_solicitud"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBoolean("activo")
        );
    }
}
