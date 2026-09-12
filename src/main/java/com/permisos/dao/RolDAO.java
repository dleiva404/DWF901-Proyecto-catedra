package com.permisos.dao;

import com.permisos.model.Rol;
import com.permisos.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RolDAO {

    private static final String SQL_LISTAR_ACTIVOS =
            "SELECT id_rol, nombre, descripcion, activo FROM roles WHERE activo = TRUE ORDER BY nombre";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id_rol, nombre, descripcion, activo FROM roles WHERE id_rol = ?";

    public List<Rol> listarActivos() throws SQLException {
        List<Rol> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_ACTIVOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Rol buscarPorId(int idRol) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    private Rol mapear(ResultSet rs) throws SQLException {
        return new Rol(
                rs.getInt("id_rol"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBoolean("activo")
        );
    }
}
