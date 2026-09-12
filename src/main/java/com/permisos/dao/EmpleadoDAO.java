package com.permisos.dao;

import com.permisos.model.Empleado;
import com.permisos.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    private static final String SQL_INSERTAR =
            "INSERT INTO empleados (nombre, apellido, dui, correo, telefono, id_sucursal_area, " +
            "id_departamento, cargo, fecha_ingreso) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_LISTAR_ACTIVOS =
            "SELECT id_empleado, nombre, apellido, dui, correo, telefono, id_sucursal_area, " +
            "id_departamento, cargo, fecha_ingreso, activo FROM empleados " +
            "WHERE activo = TRUE ORDER BY nombre, apellido";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id_empleado, nombre, apellido, dui, correo, telefono, id_sucursal_area, " +
            "id_departamento, cargo, fecha_ingreso, activo FROM empleados WHERE id_empleado = ?";

    private static final String SQL_LISTAR_POR_DEPARTAMENTO =
            "SELECT id_empleado, nombre, apellido, dui, correo, telefono, id_sucursal_area, " +
            "id_departamento, cargo, fecha_ingreso, activo FROM empleados " +
            "WHERE id_departamento = ? AND activo = TRUE ORDER BY nombre, apellido";

    private static final String SQL_ACTUALIZAR =
            "UPDATE empleados SET nombre = ?, apellido = ?, correo = ?, telefono = ?, " +
            "id_sucursal_area = ?, id_departamento = ?, cargo = ? WHERE id_empleado = ?";

    public int insertar(Empleado e) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellido());
            ps.setString(3, e.getDui());
            ps.setString(4, e.getCorreo());
            ps.setString(5, e.getTelefono());
            ps.setInt(6, e.getIdSucursalArea());
            ps.setInt(7, e.getIdDepartamento());
            ps.setString(8, e.getCargo());
            ps.setDate(9, Date.valueOf(e.getFechaIngreso()));

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo registrar el empleado.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id del empleado generado.");
    }

    public List<Empleado> listarActivos() throws SQLException {
        List<Empleado> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_ACTIVOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Empleado buscarPorId(int idEmpleado) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public List<Empleado> listarPorDepartamento(int idDepartamento) throws SQLException {
        List<Empleado> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_POR_DEPARTAMENTO)) {

            ps.setInt(1, idDepartamento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public void actualizar(Empleado e) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {

            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellido());
            ps.setString(3, e.getCorreo());
            ps.setString(4, e.getTelefono());
            ps.setInt(5, e.getIdSucursalArea());
            ps.setInt(6, e.getIdDepartamento());
            ps.setString(7, e.getCargo());
            ps.setInt(8, e.getIdEmpleado());

            ps.executeUpdate();
        }
    }

    private Empleado mapear(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
        e.setIdEmpleado(rs.getInt("id_empleado"));
        e.setNombre(rs.getString("nombre"));
        e.setApellido(rs.getString("apellido"));
        e.setDui(rs.getString("dui"));
        e.setCorreo(rs.getString("correo"));
        e.setTelefono(rs.getString("telefono"));
        e.setIdSucursalArea(rs.getInt("id_sucursal_area"));
        e.setIdDepartamento(rs.getInt("id_departamento"));
        e.setCargo(rs.getString("cargo"));

        Date fechaIngreso = rs.getDate("fecha_ingreso");
        if (fechaIngreso != null) {
            e.setFechaIngreso(fechaIngreso.toLocalDate());
        }
        e.setActivo(rs.getBoolean("activo"));
        return e;
    }
}
