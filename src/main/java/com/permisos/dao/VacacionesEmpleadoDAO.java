package com.permisos.dao;

import com.permisos.model.VacacionesEmpleado;
import com.permisos.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VacacionesEmpleadoDAO {

    private static final String SQL_BUSCAR_POR_EMPLEADO_ANIO =
            "SELECT id_vacaciones, id_empleado, anio, dias_asignados, dias_utilizados, dias_disponibles " +
            "FROM vacaciones_empleado WHERE id_empleado = ? AND anio = ?";

    private static final String SQL_DESCONTAR_DIAS =
            "UPDATE vacaciones_empleado " +
            "SET dias_utilizados = dias_utilizados + ?, dias_disponibles = dias_disponibles - ? " +
            "WHERE id_empleado = ? AND anio = ?";

    private static final String SQL_DEVOLVER_DIAS =
            "UPDATE vacaciones_empleado " +
            "SET dias_utilizados = dias_utilizados - ?, dias_disponibles = dias_disponibles + ? " +
            "WHERE id_empleado = ? AND anio = ?";

    public VacacionesEmpleado buscarPorEmpleadoYAnio(int idEmpleado, int anio) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_EMPLEADO_ANIO)) {

            ps.setInt(1, idEmpleado);
            ps.setInt(2, anio);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new VacacionesEmpleado(
                            rs.getInt("id_vacaciones"),
                            rs.getInt("id_empleado"),
                            rs.getInt("anio"),
                            rs.getInt("dias_asignados"),
                            rs.getInt("dias_utilizados"),
                            rs.getInt("dias_disponibles")
                    );
                }
            }
        }
        return null;
    }

    /** Descuenta días del saldo. Se usa cuando una solicitud de vacaciones es APROBADA. */
    public void descontarDias(int idEmpleado, int anio, int dias) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_DESCONTAR_DIAS)) {
            ps.setInt(1, dias);
            ps.setInt(2, dias);
            ps.setInt(3, idEmpleado);
            ps.setInt(4, anio);
            ps.executeUpdate();
        }
    }

    /** Devuelve días al saldo. Se usa si una solicitud ya aprobada se cancela después. */
    public void devolverDias(int idEmpleado, int anio, int dias) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_DEVOLVER_DIAS)) {
            ps.setInt(1, dias);
            ps.setInt(2, dias);
            ps.setInt(3, idEmpleado);
            ps.setInt(4, anio);
            ps.executeUpdate();
        }
    }
}
