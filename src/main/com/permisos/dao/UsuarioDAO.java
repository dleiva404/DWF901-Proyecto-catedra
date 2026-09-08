package com.permisos.dao;

import com.permisos.model.Usuario;
import com.permisos.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UsuarioDAO {

    private static final String SQL_LOGIN =
            "SELECT id_usuario, username, id_empleado, id_rol, activo, fecha_creacion, ultimo_acceso " +
            "FROM usuarios WHERE username = ? AND password = ? AND activo = TRUE";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id_usuario, username, id_empleado, id_rol, activo, fecha_creacion, ultimo_acceso " +
            "FROM usuarios WHERE id_usuario = ?";

    private static final String SQL_BUSCAR_POR_EMPLEADO =
            "SELECT id_usuario, username, id_empleado, id_rol, activo, fecha_creacion, ultimo_acceso " +
            "FROM usuarios WHERE id_empleado = ?";

    private static final String SQL_ACTUALIZAR_ULTIMO_ACCESO =
            "UPDATE usuarios SET ultimo_acceso = NOW() WHERE id_usuario = ?";

    /**
     * Valida credenciales. Devuelve null si no coinciden con ningún usuario activo.
     * Nota: en un entorno real, la contraseña debería compararse como hash (BCrypt),
     * no en texto plano.
     */
    public Usuario autenticar(String username, String password) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LOGIN)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs, username, password);
                }
            }
        }
        return null;
    }

    public Usuario buscarPorId(int idUsuario) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs, rs.getString("username"), null);
                }
            }
        }
        return null;
    }

    public Usuario buscarPorEmpleado(int idEmpleado) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_EMPLEADO)) {

            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs, rs.getString("username"), null);
                }
            }
        }
        return null;
    }

    public void registrarUltimoAcceso(int idUsuario) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR_ULTIMO_ACCESO)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    private Usuario mapear(ResultSet rs, String usernameConocido, String passwordConocido) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setUsername(usernameConocido != null ? usernameConocido : rs.getString("username"));
        u.setPassword(passwordConocido); // nunca se relee la password real desde la BD por seguridad
        u.setIdEmpleado(rs.getInt("id_empleado"));
        u.setIdRol(rs.getInt("id_rol"));
        u.setActivo(rs.getBoolean("activo"));

        Timestamp creacion = rs.getTimestamp("fecha_creacion");
        if (creacion != null) {
            u.setFechaCreacion(creacion.toLocalDateTime());
        }
        Timestamp ultimoAcceso = rs.getTimestamp("ultimo_acceso");
        if (ultimoAcceso != null) {
            u.setUltimoAcceso(ultimoAcceso.toLocalDateTime());
        }
        return u;
    }
}
