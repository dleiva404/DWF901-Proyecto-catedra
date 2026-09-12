package com.permisos.dao;

import com.permisos.model.Incapacidad;
import com.permisos.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class IncapacidadDAO {

    private static final String SQL_INSERTAR =
            "INSERT INTO incapacidades (id_solicitud, numero_documento, fecha_inicio, fecha_fin, " +
            "documento_nombre, documento_ruta, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_BUSCAR_POR_SOLICITUD =
            "SELECT id_incapacidad, id_solicitud, numero_documento, fecha_inicio, fecha_fin, " +
            "documento_nombre, documento_ruta, observaciones FROM incapacidades WHERE id_solicitud = ?";

    public int insertar(Incapacidad inc) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, inc.getIdSolicitud());
            ps.setString(2, inc.getNumeroDocumento());
            ps.setDate(3, Date.valueOf(inc.getFechaInicio()));
            ps.setDate(4, Date.valueOf(inc.getFechaFin()));
            ps.setString(5, inc.getDocumentoNombre());
            ps.setString(6, inc.getDocumentoRuta());
            ps.setString(7, inc.getObservaciones());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo registrar la incapacidad.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id de la incapacidad generada.");
    }

    public Incapacidad buscarPorSolicitud(int idSolicitud) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_SOLICITUD)) {

            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Incapacidad inc = new Incapacidad();
                    inc.setIdIncapacidad(rs.getInt("id_incapacidad"));
                    inc.setIdSolicitud(rs.getInt("id_solicitud"));
                    inc.setNumeroDocumento(rs.getString("numero_documento"));

                    Date fechaInicio = rs.getDate("fecha_inicio");
                    if (fechaInicio != null) {
                        inc.setFechaInicio(fechaInicio.toLocalDate());
                    }
                    Date fechaFin = rs.getDate("fecha_fin");
                    if (fechaFin != null) {
                        inc.setFechaFin(fechaFin.toLocalDate());
                    }
                    inc.setDocumentoNombre(rs.getString("documento_nombre"));
                    inc.setDocumentoRuta(rs.getString("documento_ruta"));
                    inc.setObservaciones(rs.getString("observaciones"));
                    return inc;
                }
            }
        }
        return null;
    }
}
