package com.permisos.dao;

import com.permisos.model.Departamento;
import com.permisos.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DepartamentoDAO {

    private static final String SQL_LISTAR_ACTIVOS =
            "SELECT id_departamento, nombre, descripcion, activo FROM departamentos " +
            "WHERE activo = TRUE ORDER BY nombre";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id_departamento, nombre, descripcion, activo FROM departamentos WHERE id_departamento = ?";

    public List<Departamento> listarActivos() throws SQLException {
        List<Departamento> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_ACTIVOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Departamento buscarPorId(int idDepartamento) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, idDepartamento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    private Departamento mapear(ResultSet rs) throws SQLException {
        return new Departamento(
                rs.getInt("id_departamento"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBoolean("activo")
        );
    }
}
