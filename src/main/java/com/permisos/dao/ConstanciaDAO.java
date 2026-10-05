package com.permisos.dao;

import com.permisos.model.Constancia;
import com.permisos.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ConstanciaDAO {

    // Inserta una nueva solicitud de constancia en la base de datos
    public boolean insertar(Constancia constancia) {
        String sql = "INSERT INTO constancias (id_empleado, tipo, institucion_destino, motivo, salario_referencia, empresa_emisora, token_verificacion, estado, fecha_solicitud) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDIENTE', NOW())";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, constancia.getIdEmpleado());
            ps.setString(2, constancia.getTipo());
            ps.setString(3, constancia.getInstitucion());
            ps.setString(4, constancia.getMotivo());
            ps.setBigDecimal(5, constancia.getSalarioReferencia());
            ps.setString(6, constancia.getEmpresaEmisora());
            ps.setString(7, constancia.getTokenVerificacion());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar constancia: " + e.getMessage());
            return false;
        }
    }

    // Busca una constancia por su ID único (necesario para generar el PDF)
    public Constancia obtenerPorId(int idConstancia) {
        String sql = "SELECT id_constancia, id_empleado, tipo, institucion_destino, motivo, salario_referencia, empresa_emisora, token_verificacion, estado, fecha_solicitud "
                + "FROM constancias WHERE id_constancia = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idConstancia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearConstancia(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar constancia por ID: " + e.getMessage());
        }
        return null;
    }

    // Busca una constancia usando el token único del código QR (para la verificación pública)
    public Constancia obtenerPorToken(String token) {
        String sql = "SELECT id_constancia, id_empleado, tipo, institucion_destino, motivo, salario_referencia, empresa_emisora, token_verificacion, estado, fecha_solicitud "
                + "FROM constancias WHERE token_verificacion = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearConstancia(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar constancia por token: " + e.getMessage());
        }
        return null;
    }

    // Obtiene todas las constancias de un empleado en específico
    public List<Constancia> obtenerPorEmpleado(int idEmpleado) {
        List<Constancia> lista = new ArrayList<>();
        String sql = "SELECT id_constancia, id_empleado, tipo, institucion_destino, motivo, salario_referencia, empresa_emisora, token_verificacion, estado, fecha_solicitud "
                + "FROM constancias WHERE id_empleado = ? ORDER BY fecha_solicitud DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearConstancia(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar constancias del empleado: " + e.getMessage());
        }
        return lista;
    }

    // Devuelve todas las constancias registradas (para el panel de administración / RRHH)
    public List<Constancia> obtenerTodas() {
        List<Constancia> lista = new ArrayList<>();
        String sql = "SELECT id_constancia, id_empleado, tipo, institucion_destino, motivo, salario_referencia, empresa_emisora, token_verificacion, estado, fecha_solicitud "
                + "FROM constancias ORDER BY fecha_solicitud DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearConstancia(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar todas las constancias: " + e.getMessage());
        }
        return lista;
    }

    // Actualiza el estado de la constancia (ej. PENDIENTE -> APROBADO o RECHAZADO)
    public boolean actualizarEstado(int idConstancia, String nuevoEstado) {
        String sql = "UPDATE constancias SET estado = ? WHERE id_constancia = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idConstancia);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar el estado: " + e.getMessage());
            return false;
        }
    }

    // Método helper para no repetir código al leer del ResultSet
    private Constancia mapearConstancia(ResultSet rs) throws SQLException {
        Constancia c = new Constancia();
        c.setIdConstancia(rs.getInt("id_constancia"));
        c.setIdEmpleado(rs.getInt("id_empleado"));
        c.setTipo(rs.getString("tipo"));
        c.setInstitucion(rs.getString("institucion_destino"));
        c.setMotivo(rs.getString("motivo"));
        c.setSalarioReferencia(rs.getBigDecimal("salario_referencia"));
        c.setEmpresaEmisora(rs.getString("empresa_emisora"));
        c.setTokenVerificacion(rs.getString("token_verificacion"));
        c.setEstado(rs.getString("estado"));

        Timestamp timestamp = rs.getTimestamp("fecha_solicitud");
        if (timestamp != null) {
            c.setFechaCreacion(new java.sql.Date(timestamp.getTime()));
        }

        return c;
    }
}