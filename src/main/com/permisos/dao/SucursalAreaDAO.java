package com.permisos.dao;

import com.permisos.model.SucursalArea;
import com.permisos.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SucursalAreaDAO {

    private static final String SQL_LISTAR_ACTIVOS =
            "SELECT id_sucursal_area, nombre, descripcion, activo FROM sucursales_areas " +
            "WHERE activo = TRUE ORDER BY nombre";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id_sucursal_area, nombre, descripcion, activo FROM sucursales_areas " +
            "WHERE id_sucursal_area = ?";

    public List<SucursalArea> listarActivos() throws SQLException {
        List<SucursalArea> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_ACTIVOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public SucursalArea buscarPorId(int idSucursalArea) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, idSucursalArea);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    private SucursalArea mapear(ResultSet rs) throws SQLException {
        return new SucursalArea(
                rs.getInt("id_sucursal_area"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBoolean("activo")
        );
    }
}
