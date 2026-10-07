package com.permisos.dao;

import com.permisos.model.HistorialSolicitud;
import com.permisos.model.Solicitud;
import com.permisos.util.JPAUtil;

import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SolicitudDAO {

<<<<<<< HEAD
    private final HistorialSolicitudDAO historialDAO = new HistorialSolicitudDAO();

    private static final String SQL_INSERTAR =
            "INSERT INTO solicitudes (id_empleado, id_tipo_solicitud, fecha_inicio, fecha_fin, " +
                    "dias_solicitados, motivo, estado) VALUES (?, ?, ?, ?, ?, ?, 'PENDIENTE')";

    private static final String SQL_LISTAR_POR_EMPLEADO =
            "SELECT id_solicitud, id_empleado, id_tipo_solicitud, fecha_solicitud, fecha_inicio, fecha_fin, " +
                    "dias_solicitados, motivo, estado, motivo_rechazo, fecha_respuesta, id_jefatura_respuesta, " +
                    "observaciones_rrhh, fecha_recepcion_rrhh " +
                    "FROM solicitudes WHERE id_empleado = ? ORDER BY fecha_solicitud DESC";

    private static final String SQL_LISTAR_PENDIENTES_POR_JEFATURA =
            "SELECT s.id_solicitud, s.id_empleado, s.id_tipo_solicitud, s.fecha_solicitud, s.fecha_inicio, " +
                    "s.fecha_fin, s.dias_solicitados, s.motivo, s.estado, s.motivo_rechazo, s.fecha_respuesta, " +
                    "s.id_jefatura_respuesta, s.observaciones_rrhh, s.fecha_recepcion_rrhh, " +
                    "CONCAT(e.nombre, ' ', e.apellido) AS nombre_empleado, " +
                    "COALESCE(e.empresa, 'Invercalma') AS nombre_empresa " +
                    "FROM solicitudes s " +
                    "JOIN empleados e ON s.id_empleado = e.id_empleado " +
                    "JOIN jefaturas j ON j.id_departamento = e.id_departamento " +
                    "WHERE j.id_empleado = ? AND j.activo = TRUE AND s.estado = 'PENDIENTE' " +
                    "ORDER BY s.fecha_solicitud ASC";

    private static final String SQL_LISTAR_TODAS_GLOBAL =
            "SELECT s.id_solicitud, s.id_empleado, s.id_tipo_solicitud, s.fecha_solicitud, s.fecha_inicio, s.fecha_fin, " +
                    "s.dias_solicitados, s.motivo, s.estado, s.motivo_rechazo, s.fecha_respuesta, s.id_jefatura_respuesta, " +
                    "s.observaciones_rrhh, s.fecha_recepcion_rrhh, " +
                    "CONCAT(e.nombre, ' ', e.apellido) AS nombre_empleado, " +
                    "COALESCE(e.empresa, 'Invercalma') AS nombre_empresa " +
                    "FROM solicitudes s " +
                    "JOIN empleados e ON s.id_empleado = e.id_empleado " +
                    "ORDER BY s.fecha_solicitud DESC";

    private static final String SQL_LISTAR_PARA_RRHH =
            "SELECT s.id_solicitud, s.id_empleado, s.id_tipo_solicitud, s.fecha_solicitud, s.fecha_inicio, s.fecha_fin, " +
                    "s.dias_solicitados, s.motivo, s.estado, s.motivo_rechazo, s.fecha_respuesta, s.id_jefatura_respuesta, " +
                    "s.observaciones_rrhh, s.fecha_recepcion_rrhh, " +
                    "CONCAT(e.nombre, ' ', e.apellido) AS nombre_empleado, " +
                    "COALESCE(e.empresa, 'Invercalma') AS nombre_empresa " +
                    "FROM solicitudes s " +
                    "JOIN empleados e ON s.id_empleado = e.id_empleado " +
                    "WHERE s.estado IN ('APROBADA','RECHAZADA') ORDER BY s.fecha_respuesta DESC";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT s.id_solicitud, s.id_empleado, s.id_tipo_solicitud, s.fecha_solicitud, s.fecha_inicio, s.fecha_fin, " +
                    "s.dias_solicitados, s.motivo, s.estado, s.motivo_rechazo, s.fecha_respuesta, s.id_jefatura_respuesta, " +
                    "s.observaciones_rrhh, s.fecha_recepcion_rrhh, " +
                    "CONCAT(e.nombre, ' ', e.apellido) AS nombre_empleado, " +
                    "COALESCE(e.empresa, 'Invercalma') AS nombre_empresa " +
                    "FROM solicitudes s " +
                    "JOIN empleados e ON s.id_empleado = e.id_empleado " +
                    "WHERE s.id_solicitud = ?";

    private static final String SQL_APROBAR =
            "UPDATE solicitudes SET estado = 'APROBADA', fecha_respuesta = NOW(), id_jefatura_respuesta = ? " +
                    "WHERE id_solicitud = ?";

    private static final String SQL_RECHAZAR =
            "UPDATE solicitudes SET estado = 'RECHAZADA', fecha_respuesta = NOW(), id_jefatura_respuesta = ?, " +
                    "motivo_rechazo = ? WHERE id_solicitud = ?";

    private static final String SQL_CANCELAR =
            "UPDATE solicitudes SET estado = 'CANCELADA' WHERE id_solicitud = ? AND estado = 'PENDIENTE'";

    private static final String SQL_MARCAR_RECIBIDA_RRHH =
            "UPDATE solicitudes SET observaciones_rrhh = ?, fecha_recepcion_rrhh = NOW() WHERE id_solicitud = ?";

=======
>>>>>>> origin/main
    public int insertar(Solicitud s) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            /*
             * La fecha de solicitud se genera aquí si todavía
             * no viene establecida.
             */
            if (s.getFechaSolicitud() == null) {
                s.setFechaSolicitud(LocalDateTime.now());
            }

            /*
             * Las solicitudes nuevas siempre comienzan PENDIENTES,
             * igual que en el INSERT JDBC original.
             */
            s.setEstado(Solicitud.ESTADO_PENDIENTE);

            em.persist(s);

            em.getTransaction().commit();

            return s.getIdSolicitud();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo registrar la solicitud.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    public List<Solicitud> listarPorEmpleado(int idEmpleado)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT s FROM Solicitud s " +
                                    "WHERE s.empleado.idEmpleado = :idEmpleado " +
                                    "ORDER BY s.fechaSolicitud DESC",
                            Solicitud.class
                    )
                    .setParameter("idEmpleado", idEmpleado)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public List<Solicitud> listarPendientesPorJefatura(int idEmpleadoJefe)
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            /*
             * La consulta JDBC original hacía:
             *
             * solicitudes -> empleados -> jefaturas
             *
             * En JPA podemos navegar por las relaciones.
             */
            return em.createQuery(
                            "SELECT s FROM Solicitud s " +
                                    "JOIN s.empleado e " +
                                    "JOIN Jefatura j ON j.departamento.idDepartamento = " +
                                    "e.departamento.idDepartamento " +
                                    "WHERE j.empleado.idEmpleado = :idEmpleadoJefe " +
                                    "AND j.activo = true " +
                                    "AND s.estado = :estado " +
                                    "ORDER BY s.fechaSolicitud ASC",
                            Solicitud.class
                    )
                    .setParameter("idEmpleadoJefe", idEmpleadoJefe)
                    .setParameter("estado", Solicitud.ESTADO_PENDIENTE)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public List<Solicitud> listarTodasGlobal() throws SQLException {
        List<Solicitud> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_TODAS_GLOBAL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Solicitud> listarParaRRHH() throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT s FROM Solicitud s " +
                                    "WHERE s.estado IN (:aprobada, :rechazada) " +
                                    "ORDER BY s.fechaRespuesta DESC",
                            Solicitud.class
                    )
                    .setParameter("aprobada", Solicitud.ESTADO_APROBADA)
                    .setParameter("rechazada", Solicitud.ESTADO_RECHAZADA)
                    .getResultList();

<<<<<<< HEAD
    public Solicitud buscarPorId(int idSolicitud) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public List<Solicitud> obtenerHistorialPorTipo(int idEmpleado, String tipoFiltro) throws SQLException {
        List<Solicitud> lista = new ArrayList<>();
        String sql = "SELECT id_solicitud, id_empleado, id_tipo_solicitud, fecha_solicitud, fecha_inicio, fecha_fin, " +
                "dias_solicitados, motivo, estado, motivo_rechazo, fecha_respuesta, id_jefatura_respuesta, " +
                "observaciones_rrhh, fecha_recepcion_rrhh " +
                "FROM solicitudes WHERE id_empleado = ? ORDER BY fecha_solicitud DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public void aprobar(int idSolicitud, int idEmpleadoJefe, int idUsuarioJefatura) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_APROBAR)) {
                ps.setInt(1, idEmpleadoJefe);
                ps.setInt(2, idSolicitud);
                ps.executeUpdate();
            }

            HistorialSolicitud h = new HistorialSolicitud();
            h.setIdSolicitud(idSolicitud);
            h.setEstadoAnterior("PENDIENTE");
            h.setEstadoNuevo("APROBADA");
            h.setIdUsuario(idUsuarioJefatura);
            historialDAO.insertar(con, h);

            con.commit();
        }
    }

    public void rechazar(int idSolicitud, int idEmpleadoJefe, String motivoRechazo, int idUsuarioJefatura)
=======
        } finally {
            em.close();
        }
    }

    public Solicitud buscarPorId(int idSolicitud)
>>>>>>> origin/main
            throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.find(Solicitud.class, idSolicitud);

        } finally {
            em.close();
        }
    }

    /**
     * Aprueba una solicitud y registra el historial
     * dentro de la misma transacción JPA.
     */
    public void aprobar(
            int idSolicitud,
            int idEmpleadoJefe,
            int idUsuarioJefatura) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Solicitud solicitud =
                    em.find(Solicitud.class, idSolicitud);

            if (solicitud == null) {
                throw new SQLException(
                        "No se encontró la solicitud."
                );
            }

            solicitud.setEstado(Solicitud.ESTADO_APROBADA);
            solicitud.setFechaRespuesta(LocalDateTime.now());
            solicitud.setIdJefaturaRespuesta(idEmpleadoJefe);

            HistorialSolicitud h = new HistorialSolicitud();

            h.setSolicitud(solicitud);
            h.setEstadoAnterior(Solicitud.ESTADO_PENDIENTE);
            h.setEstadoNuevo(Solicitud.ESTADO_APROBADA);
            h.setIdUsuario(idUsuarioJefatura);
            h.setFecha(LocalDateTime.now());

            em.persist(h);

            em.getTransaction().commit();

        } catch (SQLException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw ex;

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo aprobar la solicitud.",
                    ex
            );

        } finally {
            em.close();
        }
    }

    /**
     * Rechaza una solicitud y registra el historial
     * dentro de la misma transacción JPA.
     */
    public void rechazar(
            int idSolicitud,
            int idEmpleadoJefe,
            String motivoRechazo,
            int idUsuarioJefatura) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Solicitud solicitud =
                    em.find(Solicitud.class, idSolicitud);

            if (solicitud == null) {
                throw new SQLException(
                        "No se encontró la solicitud."
                );
            }

            solicitud.setEstado(Solicitud.ESTADO_RECHAZADA);
            solicitud.setFechaRespuesta(LocalDateTime.now());
            solicitud.setIdJefaturaRespuesta(idEmpleadoJefe);
            solicitud.setMotivoRechazo(motivoRechazo);

            HistorialSolicitud h = new HistorialSolicitud();

            h.setSolicitud(solicitud);
            h.setEstadoAnterior(Solicitud.ESTADO_PENDIENTE);
            h.setEstadoNuevo(Solicitud.ESTADO_RECHAZADA);
            h.setComentario(motivoRechazo);
            h.setIdUsuario(idUsuarioJefatura);
            h.setFecha(LocalDateTime.now());

            em.persist(h);

            em.getTransaction().commit();

        } catch (SQLException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw ex;

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo rechazar la solicitud.",
                    ex
            );

        } finally {
            em.close();
        }
    }

<<<<<<< HEAD
    public boolean cancelar(int idSolicitud, int idUsuario) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            int filas;
            try (PreparedStatement ps = con.prepareStatement(SQL_CANCELAR)) {
                ps.setInt(1, idSolicitud);
                filas = ps.executeUpdate();
=======
    /**
     * Cancela una solicitud solamente si está PENDIENTE.
     * El cambio y el historial se guardan en la misma transacción.
     */
    public boolean cancelar(
            int idSolicitud,
            int idUsuario) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Solicitud solicitud =
                    em.find(Solicitud.class, idSolicitud);

            if (solicitud == null ||
                    !Solicitud.ESTADO_PENDIENTE.equals(
                            solicitud.getEstado())) {

                em.getTransaction().rollback();
                return false;
>>>>>>> origin/main
            }

            solicitud.setEstado(Solicitud.ESTADO_CANCELADA);

            HistorialSolicitud h = new HistorialSolicitud();

            h.setSolicitud(solicitud);
            h.setEstadoAnterior(Solicitud.ESTADO_PENDIENTE);
            h.setEstadoNuevo(Solicitud.ESTADO_CANCELADA);
            h.setIdUsuario(idUsuario);
            h.setFecha(LocalDateTime.now());

            em.persist(h);

            em.getTransaction().commit();

            return true;

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo cancelar la solicitud.",
                    ex
            );

        } finally {
            em.close();
        }
    }

<<<<<<< HEAD
    public void marcarRecibidaPorRRHH(int idSolicitud, String observaciones) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_MARCAR_RECIBIDA_RRHH)) {
            ps.setString(1, observaciones);
            ps.setInt(2, idSolicitud);
            ps.executeUpdate();
        }
    }

    private Solicitud mapear(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();
        s.setIdSolicitud(rs.getInt("id_solicitud"));
        s.setIdEmpleado(rs.getInt("id_empleado"));
        s.setIdTipoSolicitud(rs.getInt("id_tipo_solicitud"));

        Timestamp fechaSolicitud = rs.getTimestamp("fecha_solicitud");
        if (fechaSolicitud != null) {
            s.setFechaSolicitud(fechaSolicitud.toLocalDateTime());
        }

        Date fechaInicio = rs.getDate("fecha_inicio");
        if (fechaInicio != null) {
            s.setFechaInicio(fechaInicio.toLocalDate());
        }
        Date fechaFin = rs.getDate("fecha_fin");
        if (fechaFin != null) {
            s.setFechaFin(fechaFin.toLocalDate());
        }

        s.setDiasSolicitados(rs.getInt("dias_solicitados"));
        s.setMotivo(rs.getString("motivo"));
        s.setEstado(rs.getString("estado"));
        s.setMotivoRechazo(rs.getString("motivo_rechazo"));

        Timestamp fechaRespuesta = rs.getTimestamp("fecha_respuesta");
        if (fechaRespuesta != null) {
            s.setFechaRespuesta(fechaRespuesta.toLocalDateTime());
        }

        int idJefaturaRespuesta = rs.getInt("id_jefatura_respuesta");
        s.setIdJefaturaRespuesta(rs.wasNull() ? null : idJefaturaRespuesta);

        s.setObservacionesRrhh(rs.getString("observaciones_rrhh"));

        Timestamp fechaRecepcionRrhh = rs.getTimestamp("fecha_recepcion_rrhh");
        if (fechaRecepcionRrhh != null) {
            s.setFechaRecepcionRrhh(fechaRecepcionRrhh.toLocalDateTime());
        }


        try {
            s.setNombreEmpleado(rs.getString("nombre_empleado"));
        } catch (Exception e) {
            s.setNombreEmpleado("Empleado #" + s.getIdEmpleado());
        }

        try {
            s.setNombreEmpresa(rs.getString("nombre_empresa"));
        } catch (Exception e) {
            s.setNombreEmpresa("Invercalma");
        }

        return s;
    }
}
=======
    /**
     * RRHH marca una solicitud ya resuelta como recibida/procesada.
     */
    public void marcarRecibidaPorRRHH(
            int idSolicitud,
            String observaciones) throws SQLException {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            Solicitud solicitud =
                    em.find(Solicitud.class, idSolicitud);

            if (solicitud != null) {
                solicitud.setObservacionesRrhh(observaciones);
                solicitud.setFechaRecepcionRrhh(
                        LocalDateTime.now()
                );
            }

            em.getTransaction().commit();

        } catch (RuntimeException ex) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new SQLException(
                    "No se pudo marcar la solicitud como recibida por RRHH.",
                    ex
            );

        } finally {
            em.close();
        }
    }
}
>>>>>>> origin/main
