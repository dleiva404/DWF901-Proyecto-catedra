package com.permisos.dao;

import com.permisos.model.Jefatura;
import com.permisos.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JefaturaDAO {

    private static final String SQL_INSERTAR =
            "INSERT INTO jefaturas (id_empleado, id_departamento, id_sucursal_area, activo) " +
            "VALUES (?, ?, ?, TRUE)";

    private static final String SQL_LISTAR_ACTIVAS =
            "SELECT id_jefatura, id_empleado, id_departamento, id_sucursal_area, activo " +
            "FROM jefaturas WHERE activo = TRUE";

    private static final String SQL_BUSCAR_POR_EMPLEADO =
            "SELECT id_jefatura, id_empleado, id_departamento, id_sucursal_area, activo " +
            "FROM jefaturas WHERE id_empleado = ? AND activo = TRUE";

    /**
     * Verifica si un empleado es jefe activo de un departamento específico.
     * Útil para validar permisos antes de dejarlo aprobar una solicitud.
     */
    private static final String SQL_ES_JEFE_DE_DEPARTAMENTO =
            "SELECT COUNT(*) FROM jefaturas " +
            "WHERE id_empleado = ? AND id_departamento = ? AND activo = TRUE";

    public int insertar(Jefatura j) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, j.getIdEmpleado());
            if (j.getIdDepartamento() != null) {
                ps.setInt(2, j.getIdDepartamento());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            if (j.getIdSucursalArea() != null) {
                ps.setInt(3, j.getIdSucursalArea());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo registrar la jefatura.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id de la jefatura generada.");
    }

    public List<Jefatura> listarActivas() throws SQLException {
        List<Jefatura> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_ACTIVAS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Jefatura> buscarPorEmpleado(int idEmpleado) throws SQLException {
        List<Jefatura> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_EMPLEADO)) {

            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public boolean esJefeDeDepartamento(int idEmpleado, int idDepartamento) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_ES_JEFE_DE_DEPARTAMENTO)) {

            ps.setInt(1, idEmpleado);
            ps.setInt(2, idDepartamento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Jefatura mapear(ResultSet rs) throws SQLException {
        Jefatura j = new Jefatura();
        j.setIdJefatura(rs.getInt("id_jefatura"));
        j.setIdEmpleado(rs.getInt("id_empleado"));

        int idDepartamento = rs.getInt("id_departamento");
        j.setIdDepartamento(rs.wasNull() ? null : idDepartamento);

        int idSucursalArea = rs.getInt("id_sucursal_area");
        j.setIdSucursalArea(rs.wasNull() ? null : idSucursalArea);

        j.setActivo(rs.getBoolean("activo"));
        return j;
    }
}
